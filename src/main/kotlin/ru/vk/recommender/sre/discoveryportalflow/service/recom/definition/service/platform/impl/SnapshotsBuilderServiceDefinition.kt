package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.platform.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.pms.writer.PmsConfpFileNameMode
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.platform.PlatformService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.BATCH_PRODUCTION_ROOT_QUEUE_NAME
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServicePmsDefinition
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueNames
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.SnapshotsBuilderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class SnapshotsBuilderServiceDefinition : PlatformService<SnapshotsBuilderServiceConfig>(
    type = ServiceType.SNAPSHOTS_BUILDER,
    serviceName = "snapshots-builder",
    configClass = SnapshotsBuilderServiceConfig::class,
    templateDirectory = "snapshots-builder",
    sourceDirectory = "recommender/platform/snapshots-builder",
    gradleBuildCommand = "recommender:platform:snapshots-builder:export",
    onecloudDirectoryName = "snapshots-builder",
    hasSecret = true,
    allowEmptySecretData = true,
    hasTesting = false,
    hasCanary = false,
) {
    override val pms: ServicePmsDefinition =
        ServicePmsDefinition(
            submitApptracer = false,
            confpFileNameMode = PmsConfpFileNameMode.TENANT_SCOPED,
        )

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: SnapshotsBuilderServiceConfig,
    ): ServiceManifestSpec {
        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 3,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 8,
            cloudRamG = 20,
            cloudLanOut = "100M",
            cloudLanIn = "100M",
            cloudVolumeSize = "200g",
            cloudAvailability = "50%",
            cloudJavaXms = "6g",
            cloudJavaXmx = "6g",
            imageName = "snapshots-builder",
            additionalEnv = mapOf(
                "SECRETS_ENV" to serviceRuntime.namespace,
                "SNAPSHOTS_BUILDER_PRODUCT" to serviceRuntime.namespace,
                "WORKER_MEMORY_TOTAL" to "52424509440",
                "YT_ENVIRONMENT" to serviceRuntime.cloudServiceName,
            ),
            ports = listOf("81", "32035"),
            maxTestingCloudPods = 0,
            maxCanaryCloudPods = 0,
            startAttemptsLimit = 100,
            enablePrometheus = false,
            enableServiceHost = false,
            pingPort = 81,
            javaMainClass = "ru.vk.recommender.snapshots.builder.SnapshotsBuilderWorkerMain",
            zenApplicationName = "snapshots-builder",
            productId = recommenderRuntime.productId
        )
    }

    override fun resolveQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings {
        return ServiceQueueSettings(
            rootQueueNames = ServiceQueueNames(
                productionRootQueueName = BATCH_PRODUCTION_ROOT_QUEUE_NAME,
                testingRootQueueName = "",
            ),
            pmsRootQueueName = recommenderRuntime
                .resolveRootQueueNames(resolveServiceScope(serviceConfig))
                .productionRootQueueName,
        )
    }

    override fun resolveTeamcityProject(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        val serviceClassName = parseNames(serviceName).className
        return "${recommenderRuntime.teamcityProjectPrefix}_Platform_SnapshotsBuilder_$serviceClassName"
    }
}
