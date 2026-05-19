package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.YtInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.CommonService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class FactorProxyServiceDefinition(
    private val ytInfoOneSecretCodec: YtInfoOneSecretCodec,
) : CommonService<RecommenderServiceConfig>(
    type = ServiceType.FACTOR_PROXY,
    serviceName = "factor-proxy",
    sourceDirectory = "recommender/public/factor-proxy",
    gradleBuildCommand = "recommender:public:factor-proxy:export",
    configClass = RecommenderServiceConfig::class,
) {
    override val hasSecret: Boolean = true

    override fun resolveQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings {
        return resolveDefaultQueueSettings(recommenderRuntime, serviceConfig).copy(
            onecloudSubqueues = listOf("factor-proxy"),
        )
    }

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceManifestSpec {
        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
            javaMainClass = "ru.vk.recommender.factorproxy.FactorProxyMain",
            defaultCloudPods = 5,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 8,
            cloudRamG = 16,
            cloudLanOut = "300M",
            cloudLanIn = "400M",
            cloudVolumeSize = "50g",
            cloudAvailability = "80%",
            cloudJavaXms = "8g",
            cloudJavaXmx = "8g",
            extraPorts = listOf("40195"),
            maxTestingCloudPods = 1,
            maxCanaryCloudPods = 3,
            productId = recommenderRuntime.productId
        )
    }

    override fun buildSecretPairs(
        taskContext: OneSecretTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): Map<String, String> {
        return ytInfoOneSecretCodec.toSecretData(taskContext.ytOffline)
    }
}
