package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.utils

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.context.TeamcityProjectsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.ProjectParameters

object NameUtils {
    private const val PUBLIC_PRODUCTION_ROOT_QUEUE_NAME_SUFFIX = "public.app.production.recommender.prod"

    fun formProjectId(serviceName: String): String {
        return serviceName.split("-")
            .joinToString("") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
            .replace(" ", "")
    }

    fun formProjectName(serviceName: String): String {
        val name = serviceName.lowercase()

        return name.replaceFirst("public-", "")
            .replace("-", " ")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    fun formProjectIdWithParent(
        parentId: String,
        subprojectName: String,
    ) = "${parentId}_${formProjectId(subprojectName)}"


    fun buildProjectParameters(
        taskContext: TeamcityProjectsTaskContext,
        serviceRuntime: ServiceRuntime,
    ): ProjectParameters {
        return ProjectParameters(
            branchFilter = "${serviceRuntime.cloudServiceName}-release-.*",
            deployCloudSubqueue = buildDeploySubqueuePath(serviceRuntime.serviceProductionRootQueueName),
            drillsProjects = "public,${taskContext.recommenderName}",
            teamsChatId = taskContext.teamsChatId,
            servicesToDeploy = serviceRuntime.distinctOnecloudSubqueues().joinToString(","),
            shootingGraph = serviceRuntime.cloudServiceName,
            shootingСomponent = "recommend-${taskContext.recommenderName}",
        )
    }

    fun buildTeamcitySubprojectNames(taskContext: TeamcityProjectsTaskContext): List<String> {
        val projectName = taskContext.projectName
        return if (projectName.isNullOrBlank()) {
            listOf(taskContext.recommenderClassName)
        } else {
            listOf(
                parseNames(projectName).className,
                taskContext.recommenderClassName,
            )
        }
    }

    private fun buildDeploySubqueuePath(serviceProductionRootQueueName: String): String {
        if (!serviceProductionRootQueueName.endsWith(PUBLIC_PRODUCTION_ROOT_QUEUE_NAME_SUFFIX)) {
            return ""
        }

        val queuePrefix = serviceProductionRootQueueName
            .removeSuffix(PUBLIC_PRODUCTION_ROOT_QUEUE_NAME_SUFFIX)
            .trimEnd('.')
        if (queuePrefix.isBlank()) {
            return ""
        }

        val pathSegments = queuePrefix.split('.')
            .filter { segmentName -> segmentName.isNotBlank() }
            .asReversed()
        return if (pathSegments.isEmpty()) {
            ""
        } else {
            "${pathSegments.joinToString("/")}/"
        }
    }
}
