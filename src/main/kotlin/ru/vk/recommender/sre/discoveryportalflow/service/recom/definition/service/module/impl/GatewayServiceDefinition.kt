package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.module.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.support.Log4jTemplateSupport
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.module.ModuleService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class GatewayServiceDefinition : ModuleService<RecommenderServiceConfig>(
    type = ServiceType.GATEWAY,
    serviceName = "gateway",
    configClass = RecommenderServiceConfig::class,
) {
    override fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): Map<String, String> {
        return buildMap {
            putAll(super.resolveTemplateReplacements(recommenderRuntime, serviceRuntime, serviceConfig))
            put("\${DockerfileDragonflyFragment}", Log4jTemplateSupport.dragonflyDockerfileFragment())
        }
    }

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
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
            cloudVolumeSize = "200g",
            cloudAvailability = "80%",
            cloudJavaXms = "8g",
            cloudJavaXmx = "8g",
            additionalEnv = recommenderEnvironment,
            additionalTestingEnv = recommenderEnvironment,
            extraPorts = listOf("40195"),
            maxTestingCloudPods = 1,
            productId = recommenderRuntime.productId
        )
    }
}
