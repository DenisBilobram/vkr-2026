package ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapProjectRecommenderContextTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomServiceRegistry
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.PostProcessedConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.ProjectRecommenderConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.WorkspaceResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames
import ru.vk.recommender.sre.discoveryportalflow.service.registry.context.FillProjectRegistryTaskContext

@Component
class ProjectRecommenderRuntimeContextFactory(
    private val recomServiceRegistry: RecomServiceRegistry,
) {

    fun build(stageContext: BootstrapProjectRecommenderContextTaskContext): FillProjectRegistryTaskContext {
        val projectConfig = stageContext.projectRecommender
        val projectName = projectConfig.projectName

        val syntheticRuntime = buildSyntheticRecommenderRuntime(projectConfig, stageContext)

        val serviceRuntimes = stageContext.services.map { serviceConfig ->
            val service = recomServiceRegistry.serviceByType(serviceConfig.type)
            ServiceRuntime.create(
                recommenderRuntime = syntheticRuntime,
                service = service,
                serviceConfig = serviceConfig,
            )
        }

        serviceRuntimes
            .filter { serviceRuntime -> serviceRuntime.config is PostProcessedConfig }
            .forEach { serviceRuntime -> (serviceRuntime.config as PostProcessedConfig).postProcess(serviceRuntimes) }

        val projectScopedServices = serviceRuntimes.filter { serviceRuntime ->
            serviceRuntime.scope == ServiceScope.PROJECT_SCOPED
        }

        val projectRecommenderRuntime = ProjectRecommenderRuntime(
            projectName = projectName,
            productId = projectConfig.productId,
            serviceOwner = projectConfig.serviceOwner,
            productionRootQueueName = "${projectName.lowercase()}.$PUBLIC_PROD_QUEUE",
            testingRootQueueName = "${projectName.lowercase()}.$PUBLIC_TEST_QUEUE",
            services = projectScopedServices,
        )

        return FillProjectRegistryTaskContext(
            projectRecommender = projectRecommenderRuntime,
            dcSettings = stageContext.dcSettings,
        )
    }

    private fun buildSyntheticRecommenderRuntime(
        projectConfig: ProjectRecommenderConfig,
        stageContext: BootstrapProjectRecommenderContextTaskContext,
    ): RecommenderRuntime {
        val projectName = projectConfig.projectName
        val projectQueueProd = "${projectName.lowercase()}.$PUBLIC_PROD_QUEUE"
        val projectQueueTest = "${projectName.lowercase()}.$PUBLIC_TEST_QUEUE"
        return RecommenderRuntime(
            workspaceRoot = WorkspaceResolver.workspaceRoot,
            recommenderName = projectName,
            serviceOwner = projectConfig.serviceOwner,
            additionalResponsibles = emptyList(),
            additionalFollowers = emptyList(),
            recomOneSecretId = null,
            projectOneSecretId = null,
            productId = projectConfig.productId,
            projectProductId = projectConfig.productId,
            vertical = parseNames(projectName).folderName,
            dictionaryBaseProject = projectName,
            projectName = projectName,
            createMinOneCloudConfiguration = false,
            recommenderRoot = "recommender/public/$projectName/",
            teamcityProjectPrefix = "Public_Recommender_${parseNames(projectName).className}",
            projectProductionRootQueueName = projectQueueProd,
            projectTestingRootQueueName = projectQueueTest,
            productionRootQueueName = projectQueueProd,
            testingRootQueueName = projectQueueTest,
            i2iProductionRootQueueName = "$projectName-i2i.$projectQueueProd",
            i2iTestingRootQueueName = "$projectName-i2i.$projectQueueTest",
            names = parseNames(projectName),
            clusterName = stageContext.servicehostClusterName ?: projectName,
        )
    }

    private companion object {
        const val PUBLIC_PROD_QUEUE = "public.app.production.recommender.prod"
        const val PUBLIC_TEST_QUEUE = "public.app.testing.recommender.prod"
    }
}
