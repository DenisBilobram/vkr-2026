package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.module.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.support.Log4jTemplateSupport
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.module.ModuleService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class BaseServiceDefinition : ModuleService<BaseServiceConfig>(
    type = ServiceType.BASE,
    serviceName = "base",
    configClass = BaseServiceConfig::class,
) {

    override fun resolveQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings {
        val baseServiceConfig = typedConfig(serviceConfig)
        return resolveDefaultQueueSettings(recommenderRuntime, serviceConfig).copy(
            onecloudSubqueues = (0 until baseServiceConfig.shardsCount).map { shardIndex -> "shard${shardIndex}-java" },
        )
    }

    override fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: BaseServiceConfig,
    ): Map<String, String> {
        return buildMap {
            putAll(super.resolveTemplateReplacements(recommenderRuntime, serviceRuntime, serviceConfig))
            put("\${ShardsAmount}", serviceConfig.shardsCount.toString())
            put("\${DockerfileDragonflyFragment}", Log4jTemplateSupport.dragonflyDockerfileFragment())
            put("\${Log4jFeaturesAppenderBlock}", Log4jTemplateSupport.featuresAppenderBlock())
            put("\${Log4jFunnelAppenderBlock}", Log4jTemplateSupport.funnelAppenderBlock())
            put("\${Log4jFeaturesLoggerBlock}", Log4jTemplateSupport.featuresLoggerBlock())
            put("\${Log4jFunnelLoggerBlock}", Log4jTemplateSupport.funnelLoggerBlock())
        }
    }

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: BaseServiceConfig,
    ): ServiceManifestSpec {
        val shardsCount = serviceConfig.shardsCount
        val recommenderEnvironment = OnecloudServiceManifestBuilder.buildRecommenderEnvironment(recommenderRuntime)

        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 3,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 2,
            cloudRamG = 9,
            cloudLanOut = "100M",
            cloudLanIn = "100M",
            cloudVolumeSize = "75G",
            cloudAvailability = "66%",
            cloudJavaXms = "4g",
            cloudJavaXmx = "4g",
            additionalEnv = recommenderEnvironment + mapOf(
                "SHARDS_AMOUNT" to shardsCount.toString(),
                "USE_FORMULA_SNAPSHOT" to "true",
            ),
            additionalTestingEnv = recommenderEnvironment + mapOf(
                "SHARDS_AMOUNT" to shardsCount.toString(),
                "USE_FORMULA_SNAPSHOT" to "true",
            ),
            extraPorts = listOf("3443", "8080", "40195"),
            maxTestingCloudPods = 1,
            productId = recommenderRuntime.productId
        )
    }
}
