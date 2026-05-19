package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.RedisInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.CommonService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.MediatorServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class MediatorServiceDefinition(
    private val redisInfoOneSecretCodec: RedisInfoOneSecretCodec,
) : CommonService<MediatorServiceConfig>(
    type = ServiceType.MEDIATOR,
    serviceName = "mediator",
    sourceDirectory = "recommender/public/mediator",
    gradleBuildCommand = "recommender:public:mediator:export",
    configClass = MediatorServiceConfig::class,
) {
    override val hasSecret: Boolean = true

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: MediatorServiceConfig,
    ): ServiceManifestSpec {
        val recommenderEnvironment = OnecloudServiceManifestBuilder.buildRecommenderEnvironment(recommenderRuntime)

        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            serviceRuntime = serviceRuntime,
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
            additionalEnv = recommenderEnvironment,
            additionalTestingEnv = recommenderEnvironment,
            extraPorts = listOf("40195"),
            maxTestingCloudPods = 1,
            maxCanaryCloudPods = 3,
            productId = recommenderRuntime.productId
        )
    }

    override fun buildSecretPairs(
        taskContext: OneSecretTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: MediatorServiceConfig,
    ): Map<String, String> {
        return redisInfoOneSecretCodec.toSecretData(taskContext.redis)
    }

    override fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: MediatorServiceConfig,
    ): Map<String, String> {
        val cacheImports = if (serviceConfig.withCache) {
            """
            import ru.vk.recommender.common.cache.dao.util.CacheKeyComputer;
            import ru.vk.recommender.common.cache.dao.util.RedisKeyUtil;
            """.trimIndent()
        } else {
            ""
        }
        val cacheBean = if (serviceConfig.withCache) {
            """
                @Bean
                public CacheKeyComputer cacheKeyComputer() {
                    return (ctx, rt) ->
                            RedisKeyUtil.createKeyWithParam(ctx.getStrongestUserId(), ctx.getPadId(), rt);
                }

            """.trimIndent()
        } else {
            ""
        }

        return buildMap {
            putAll(super.resolveTemplateReplacements(recommenderRuntime, serviceRuntime, serviceConfig))
            put("\${MediatorFolder}", serviceRuntime.names.folderName.removePrefix("vk-"))
            put("\${MediatorCacheImports}", cacheImports)
            put("\${MediatorCacheBean}", cacheBean)
            put("\${MediatorCacheEnabled}", serviceConfig.withCache.toString())
        }
    }
}
