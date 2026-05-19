package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.YtInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.pms.context.ServicePmsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.pms.service.submit.ServicePmsSubmitServiceOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.CommonService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class YtProxyServiceDefinition(
    private val ytInfoOneSecretCodec: YtInfoOneSecretCodec,
) : CommonService<RecommenderServiceConfig>(
    type = ServiceType.YT_PROXY,
    serviceName = "yt-proxy",
    sourceDirectory = "recommender/public/yt-proxy",
    gradleBuildCommand = "recommender:public:yt-proxy:export",
    configClass = RecommenderServiceConfig::class,
) {
    override val hasSecret: Boolean = true

    override fun submitPmsConfig(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServicePmsSubmitServiceOutcome {
        val submitOutcome = super.submitPmsConfig(taskContext, serviceRuntime, serviceConfig)
        pmsSupport.submitYtProxyConfig(taskContext, serviceRuntime)
        return submitOutcome
    }

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceManifestSpec {
        val ytProxyEnvironment = mapOf("YT_PROXY_SERVICE_NAME" to serviceRuntime.namespace)

        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 5,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 4,
            cloudRamG = 20,
            cloudLanOut = "200M",
            cloudLanIn = "300M",
            cloudVolumeSize = "100g",
            cloudAvailability = "66%",
            cloudJavaXms = "12g",
            cloudJavaXmx = "12g",
            additionalEnv = ytProxyEnvironment,
            additionalTestingEnv = ytProxyEnvironment,
            extraCanaryEnv = mapOf("SECRETS_ENV" to "canary"),
            maxTestingCloudPods = 1,
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
