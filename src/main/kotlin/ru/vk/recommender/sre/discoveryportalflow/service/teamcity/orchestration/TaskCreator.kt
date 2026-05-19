package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.orchestration

import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createBuildDockerImageOnecloudBuild
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createCherryPickBuild
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createCloseReleaseBuild
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createCreateReleaseBuild
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createDeployRecommenderCloudBuild
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createDeployToProductionButton
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createGitlabCITestApphostGraphs
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createTestApphostGraphs
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds.createTestApphostGraphsByToggles
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.client.TeamCityClient
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.dependencies.chainBuilds
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.dependencies.setDependency
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.Environment
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.ProjectParameters
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.TriggeredBuild
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.utils.DEFAULT_PARENT_PROJECT
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.utils.NameUtils.formProjectIdWithParent
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.utils.NameUtils.formProjectName

class TaskCreator(
    val client: TeamCityClient,
) {
    /**
     * @param serviceName       Имя сервиса в облаке. Будет создан проект с таким именем
     * @param subprojectNames   List of subproject names under the public recommender root.
     * Example: [SRE, TestProject] -> Public -> Recommender -> SRE -> TestProject -> <serviceName>
     * Если подпроект уже есть - создание будет скипнуто.
     * @param prodDcs           Датацентры для раскатки на прод
     * @param testDcs           Датацентры для раскатки на тестинг
     * @param canaryDcs         Датацентры для раскатки на канарейку
     * @param additionalProps   Дополнительные параметры. Задаются на весь проект <serviceName>
     * @param branch            Имя релизной ветки. Если задано, то будет запущено создание докер-образа (первый шаг) чейна
     */
    fun createServiceProject(
        serviceName: String,
        subprojectNames: List<String> = emptyList(),
        subprojectString: String? = null,
        prodDcs: List<String>,
        testDcs: List<String> = emptyList(),
        canaryDcs: List<String> = emptyList(),
        additionalProps: ProjectParameters = ProjectParameters(),
        branch: String?,
    ): TriggeredBuild {
        // Проходим по списку подпроектов, создавая иерархию, если не передали готовый проект
        val parentProjectId = subprojectString ?: createProjectsHierarchy(subprojectNames, DEFAULT_PARENT_PROJECT)

        // ID и имя основного проекта сервиса
        val projectId = formProjectIdWithParent(parentProjectId, serviceName)
        val projectName = formProjectName(serviceName)

        // Создаём проект сервиса
        client.createSubProject(projectId, projectName, parentProjectId)

        // Добавляем дополнительные параметры
        if (additionalProps.toMap().isNotEmpty()) {
            client.addProjectParameters(
                projectId,
                additionalProps.toMap()
            )
        }

        // Список всех сборок
        val allBuilds = mutableListOf<Map<String, Any>>()

        // 1. Сборка Docker-образа
        val dockerBuild = createBuildDockerImageOnecloudBuild(projectId, serviceName)

        // 2. Цепочка деплоя
        val chainBuildsList = mutableListOf<MutableMap<String, Any>>()
        val dockerBuildMutable = dockerBuild.toMutableMap()
        chainBuildsList.add(dockerBuildMutable)

        val lastTestDeploy = createDeployTask(chainBuildsList, testDcs, projectId, serviceName, Environment.TESTING)
        createDeployTask(chainBuildsList, canaryDcs, projectId, serviceName, Environment.CANARY)
        createDeployTask(chainBuildsList, prodDcs, projectId, serviceName, Environment.PRODUCTION)

        // Связываем цепочку
        chainBuilds(chainBuildsList)
        allBuilds.addAll(chainBuildsList)

        // 3. Тесты apphost (если есть тестовые DC)
        createApphostTests(testDcs, projectId, allBuilds, lastTestDeploy)

        // 4. Вспомогательные сборки
        allBuilds.add(createCreateReleaseBuild(projectId, serviceName))
        allBuilds.add(createCloseReleaseBuild(projectId, serviceName))
        allBuilds.add(createCherryPickBuild(projectId))
        allBuilds.add(createGitlabCITestApphostGraphs(projectId))

        // Создаём все сборки
        client.createBuilds(allBuilds)

        println("🎉 All builds for project $projectId have been created successfully.\n")

        val dockerBuildBranch = branch ?: "master"
        val dockerBuildId = dockerBuild["id"]!!.toString()
        val queuedId = client.triggerBuild(dockerBuildId, dockerBuildBranch)
        if (queuedId != null) {
            println("🎉 First build (Docker image) is queued with ID: $queuedId\n")
            return TriggeredBuild(dockerBuildId, dockerBuildBranch, queuedId)
        } else {
            println("❌ Failed to trigger the first build.\n")
            throw RuntimeException("Failed to trigger the first build.")
        }
    }

    /**
     * @param serviceName       Имя сервиса в облаке. Изменения будут в проекте с соответствующим именем
     * Example: [SRE, TestProject] -> Public -> Recommender -> SRE -> TestProject -> <serviceName>
     * Требуется ввести существующую иерархию.
     * @param prodDcs           Датацентры для раскатки на прод
     * @param testDcs           Датацентры для раскатки на тестинг
     * @param canaryDcs         Датацентры для раскатки на канарейку
     */
    fun addDeployBuildToExistingService(
        serviceName: String,
        projectId: String,
        prodDcs: List<String>,
        testDcs: List<String> = emptyList(),
        canaryDcs: List<String> = emptyList(),
        additionalProps: ProjectParameters,
        parallelDeploy: Boolean = false,
        dockerBuildIdToChain: String? = null,
    ) {
        if (!client.checkIsProjectAlreadyExist(projectId)) {
            throw IllegalArgumentException(
                """
                Проекта $projectId не существует.
                """.trimIndent()
            )
        }

        if (additionalProps.toMap().isNotEmpty()) {
            client.addProjectParameters(projectId, additionalProps.toMap())
        }

        // Список всех сборок
        val allBuilds = mutableListOf<Map<String, Any>>()

        val dockerBuildId =
            dockerBuildIdToChain ?: createBuildDockerImageOnecloudBuild(projectId, serviceName)["id"].toString()

        // 2. Цепочка деплоя
        val chainBuildsList = mutableListOf<MutableMap<String, Any>>()

        val lastTestDeploy = createDeployTask(chainBuildsList, testDcs, projectId, serviceName, Environment.TESTING)
        createDeployTask(chainBuildsList, canaryDcs, projectId, serviceName, Environment.CANARY)
        createDeployTask(chainBuildsList, prodDcs, projectId, serviceName, Environment.PRODUCTION)

        // Если параллельная раскатка нам не нужно связывать деплой, просто цепляем все к билду
        if (parallelDeploy) {
            val deployToProductionButton = createDeployToProductionButton(projectId).toMutableMap()

            for (chainBuild in chainBuildsList) {
                setDependency(chainBuild, dockerBuildId)
                setDependency(deployToProductionButton, chainBuild, true)
            }
            allBuilds.addAll(chainBuildsList)
            allBuilds.add(deployToProductionButton)
        } else {
            // Связываем цепочку
            chainBuilds(chainBuildsList)
            allBuilds.addAll(chainBuildsList)

            // 3. Тесты apphost (если есть тестовые DC)
            createApphostTests(testDcs, projectId, allBuilds, lastTestDeploy)

            // Добавляем к первому элементу получившегося списку зависимость на докер образ
            setDependency(chainBuildsList.first(), dockerBuildId)
        }

        // Создаём все сборки
        client.createBuilds(allBuilds)

        println("🎉 All builds for project $projectId have been created successfully.\n")

    }

    private fun createProjectsHierarchy(
        subprojectNames: List<String>,
        parentProjectId: String,
    ): String {
        var parentProjectId1 = parentProjectId
        for (subName in subprojectNames) {
            val subProjectId = formProjectIdWithParent(parentProjectId1, subName)
            val subProjectName = formProjectName(subName)
            // Создаём подпроект (метод идемпотентен)
            client.createSubProject(subProjectId, subProjectName, parentProjectId1)
            parentProjectId1 = subProjectId
        }
        return parentProjectId1
    }

    private fun createDeployTask(
        chainBuildsList: MutableList<MutableMap<String, Any>>,
        dcs: List<String>,
        projectId: String,
        serviceName: String,
        environment: Environment,
    ): Map<String, Any>? {
        var lastBuild: Map<String, Any>? = null
        for (dc in dcs) {
            val build = createDeployRecommenderCloudBuild(
                projectId,
                serviceName,
                dc,
                environment,
            ).toMutableMap()
            chainBuildsList.add(build)
            lastBuild = build
        }

        return lastBuild
    }

    private fun createApphostTests(
        testDcs: List<String>,
        projectId: String,
        allBuilds: MutableList<Map<String, Any>>,
        lastTestDeploy: Map<String, Any>?,
    ) {
        if (testDcs.isNotEmpty()) {
            val testApphost = createTestApphostGraphs(projectId)

            allBuilds.add(testApphost)
            if (lastTestDeploy != null) {
                val testApphostMutable = testApphost.toMutableMap()
                setDependency(testApphostMutable, lastTestDeploy)
                allBuilds[allBuilds.lastIndex] = testApphostMutable
            }
            val testByToggles = createTestApphostGraphsByToggles(projectId)

            allBuilds.add(testByToggles)
        }
    }

    fun addComponents(components: List<String>) {
        println("Create task for adding components to Jira: ${components.joinToString()}}\n")
        val taskId = client.triggerAddingComponent(components)
        println("Task for adding components created with id: $taskId\n")
    }
}
