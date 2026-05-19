package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.CommonService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class MetaI2IServiceDefinition : CommonService<RecommenderServiceConfig>(
    type = ServiceType.META_I2I,
    serviceName = "i2i-meta",
    sourceDirectory = "recommender/public/i2i-meta",
    gradleBuildCommand = "recommender:public:i2i-meta:export",
    configClass = RecommenderServiceConfig::class,
    defaultScope = ServiceScope.I2I_VERTICAL_SCOPED,
    hasTesting = false,
    hasCanary = false,
){

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceManifestSpec {
        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 2,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 4,
            cloudRamG = 8,
            cloudLanOut = "300M",
            cloudLanIn = "600M",
            cloudVolumeSize = "100g",
            cloudAvailability = "80%",
            cloudJavaXms = "6g",
            cloudJavaXmx = "6g",
            extraPorts = listOf("40195"),
            maxTestingCloudPods = 0,
            maxCanaryCloudPods = 0,
            productId = recommenderRuntime.productId
        )
    }
}
