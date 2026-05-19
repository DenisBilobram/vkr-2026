package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.CommonService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseI2IServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class BaseI2IServiceDefinition : CommonService<BaseI2IServiceConfig>(
    type = ServiceType.BASE_I2I,
    serviceName = "i2i-base",
    sourceDirectory = "recommender/platform/i2i/base",
    gradleBuildCommand = "recommender:platform:i2i:base:export",
    configClass = BaseI2IServiceConfig::class,
    defaultScope = ServiceScope.I2I_VERTICAL_SCOPED,
    hasTesting = false,
    hasCanary = false,
) {

    override fun resolveQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings {
        val baseI2IServiceConfig = typedConfig(serviceConfig)
        return resolveDefaultQueueSettings(recommenderRuntime, serviceConfig).copy(
            onecloudSubqueues = (0 until baseI2IServiceConfig.shardsCount).map { shardIndex -> "shard${shardIndex}-java" },
        )
    }

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: BaseI2IServiceConfig,
    ): ServiceManifestSpec {
        val shardsCount = serviceConfig.shardsCount
        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 1,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 1,
            cloudRamG = 10,
            cloudLanOut = "100M",
            cloudLanIn = "100M",
            cloudVolumeSize = "100g",
            cloudAvailability = "80%",
            cloudJavaXms = "4g",
            cloudJavaXmx = "4g",
            additionalEnv = mapOf(
                "SHARDS_AMOUNT" to shardsCount.toString(),
                "VERTICAL" to "${serviceRuntime.names.folderName}_i2i_base",
            ),
            additionalTestingEnv = mapOf(
                "SHARDS_AMOUNT" to shardsCount.toString(),
                "VERTICAL" to "${serviceRuntime.names.folderName}_i2i_base",
            ),
            extraPorts = listOf("3443", "40195"),
            maxTestingCloudPods = 1,
            maxCanaryCloudPods = 0,
            productId = recommenderRuntime.productId
        )
    }
}
