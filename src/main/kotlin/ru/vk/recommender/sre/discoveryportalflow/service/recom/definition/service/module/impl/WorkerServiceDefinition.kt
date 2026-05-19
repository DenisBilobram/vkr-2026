package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.module.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.module.ModuleService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.BATCH_PRODUCTION_ROOT_QUEUE_NAME
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueNames
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.WorkerServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class WorkerServiceDefinition : ModuleService<WorkerServiceConfig>(
    type = ServiceType.WORKER,
    serviceName = "worker",
    configClass = WorkerServiceConfig::class,
    hasTesting = false,
    hasCanary = false,
) {
    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: WorkerServiceConfig,
    ): ServiceManifestSpec {
        val packageNames = parseNames(serviceRuntime.namespace)

        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 18,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            javaMainClass = "ru.vk.ai.discovery.${packageNames.packageName}.worker.${packageNames.className}WorkerMain",
            cloudCpuVcores = 8,
            cloudRamG = 20,
            cloudLanOut = "100M",
            cloudLanIn = "100M",
            cloudVolumeSize = "200g",
            cloudAvailability = "50%",
            cloudJavaXms = "8g",
            cloudJavaXmx = "8g",
            additionalEnv = mapOf(
                "WORKER_MEMORY_TOTAL" to "51539607552",
                "YT_ENVIRONMENT" to serviceRuntime.namespace,
                "ADDITIONAL_JAVA_OPTIONS" to "Dyt.cluster.proxies=jupiter:http-proxy-public.jupiter-yt.nda.example.invalid",
                "ITEM_EMBEDDINGS_CLUSTER_CONFIG" to "saturn",
            ),
            enableApptracer = false,
            ports = listOf("81", "32035"),
            maxTestingCloudPods = 0,
            maxCanaryCloudPods = 0,
            serviceWithSnapshot = false,
            enablePrometheus = false,
            enableServiceHost = false,
            pingPort = 81,
            productId = recommenderRuntime.productId
        )
    }

    override fun resolveGradleBuildCommand(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        val workerServiceConfig = typedConfig(serviceConfig)
        return resolveScopedGradleBuildCommand(
            recommenderRuntime = recommenderRuntime,
            serviceScope = resolveServiceScope(workerServiceConfig),
            scopedServiceName = resolveCloudServiceName(recommenderRuntime, workerServiceConfig),
        )
    }

    override fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: WorkerServiceConfig,
    ): Map<String, String> {
        val defaultWorkerPool = serviceConfig.defaultPool
            ?: parseNames(serviceRuntime.namespace).packageName

        return super.resolveTemplateReplacements(
            recommenderRuntime = recommenderRuntime,
            serviceRuntime = serviceRuntime,
            serviceConfig = serviceConfig,
        ) + mapOf(
            "\${YtEnvironmentName}" to serviceRuntime.namespace,
            "\${YtRootName}" to parseNames(serviceRuntime.namespace).folderName,
            "\${YtRobotName}" to "robot-public-${serviceRuntime.namespace}-yt-offline",
            "\${YtDefaultPool}" to defaultWorkerPool,
            "\${YtStage}" to "public-test",
        )
    }

    override fun resolveTeamcityProject(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        val workerServiceConfig = typedConfig(serviceConfig)
        return resolveScopedTeamcityProject(
            recommenderRuntime = recommenderRuntime,
            serviceScope = resolveServiceScope(workerServiceConfig),
            serviceClassName = parseNames(resolveCloudServiceName(recommenderRuntime, workerServiceConfig)).className,
        )
    }

    override fun resolveQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings {
        val workerServiceConfig = typedConfig(serviceConfig)
        return ServiceQueueSettings(
            rootQueueNames = ServiceQueueNames(
                productionRootQueueName = BATCH_PRODUCTION_ROOT_QUEUE_NAME,
                testingRootQueueName = "",
            ),
            pmsRootQueueName = recommenderRuntime
                .resolveRootQueueNames(resolveServiceScope(workerServiceConfig))
                .productionRootQueueName,
            onecloudSubqueues = listOf("worker-java"),
        )
    }
}
