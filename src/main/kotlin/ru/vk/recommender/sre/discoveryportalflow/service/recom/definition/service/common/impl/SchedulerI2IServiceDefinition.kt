package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.uniqueProductionDatacenters
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.YtInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.CommonService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.SchedulerI2IServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class SchedulerI2IServiceDefinition(
    private val ytInfoOneSecretCodec: YtInfoOneSecretCodec,
) : CommonService<SchedulerI2IServiceConfig>(
    type = ServiceType.SCHEDULER_I2I,
    serviceName = "i2i-scheduler",
    sourceDirectory = "recommender/public/i2i-scheduler",
    gradleBuildCommand = "recommender:public:i2i-scheduler:export",
    configClass = SchedulerI2IServiceConfig::class,
    defaultScope = ServiceScope.I2I_VERTICAL_SCOPED,
    hasTesting = false,
    hasCanary = false,
) {
    override val hasSecret: Boolean = true

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: SchedulerI2IServiceConfig,
    ): ServiceManifestSpec {
        val factorProxyScopeName = if (serviceConfig.factorProxyScope == ServiceScope.PROJECT_SCOPED)
            recommenderRuntime.projectName else recommenderRuntime.recommenderName
        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 1,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 4,
            cloudRamG = 12,
            cloudLanOut = "200M",
            cloudLanIn = "200M",
            cloudVolumeSize = "50g",
            cloudAvailability = "50%",
            cloudJavaXms = "10g",
            cloudJavaXmx = "10g",
            additionalEnv = mapOf(
                "I2I_SCHEDULER_FACTOR_PROXY_WEIGHTED_URLS" to
                        dcSettings.uniqueProductionDatacenters().joinToString(",") {
                            "factor-proxy.${factorProxyScopeName}-factor-proxy.${it.lowercase()}.nda.example.invalid:1.0"
                        },
                "I2I_SCHEDULER_GRAPH_WEIGHTED_URLS" to
                        dcSettings.uniqueProductionDatacenters().joinToString(",") {
                            "sh.gateway-${recommenderRuntime.clusterName}.${it.lowercase()}.nda.example.invalid:1.0"
                        },
                "I2I_SCHEDULER_RECOMMENDER_NAME" to "prefetch",
                "I2I_SCHEDULER_SHARDS_COUNT" to "1024",
                "I2I_SCHEDULER_MAX_SHARDS_PER_OWNER" to "256",
                "I2I_SCHEDULER_RECOMMENDER_QUEUE_SIZE" to "512000",
                "I2I_SCHEDULER_ACTIVE_DATACENTERS" to dcSettings.uniqueProductionDatacenters().joinToString(";"),
                "I2I_SCHEDULER_FACTOR_PROXY_IO_THREADS_COUNT" to "8",
                "I2I_SCHEDULER_RECOMMENDER_IO_THREADS_COUNT" to "16",
                "I2I_SCHEDULER_RECOMMENDER_THREADS_COUNT" to "16",
                "MAX_DIRECT_MEMORY" to "1",
                "YT_ENVIRONMENT" to serviceRuntime.names.packageName,
            ),
            ports = listOf("81"),
            maxTestingCloudPods = 0,
            maxCanaryCloudPods = 0,
            startAttemptsLimit = 100,
            pingPort = 81,
            productId = recommenderRuntime.productId
        )
    }

    override fun buildSecretPairs(
        taskContext: OneSecretTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: SchedulerI2IServiceConfig,
    ): Map<String, String> {
        return ytInfoOneSecretCodec.toSecretData(taskContext.ytOffline)
    }
}
