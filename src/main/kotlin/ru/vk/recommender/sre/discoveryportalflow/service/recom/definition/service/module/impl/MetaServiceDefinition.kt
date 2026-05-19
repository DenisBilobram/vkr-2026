package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.module.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
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
class MetaServiceDefinition : ModuleService<RecommenderServiceConfig>(
    type = ServiceType.META,
    serviceName = "meta",
    configClass = RecommenderServiceConfig::class,
) {
    override fun generateCode(
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
        gitlabProjectClient: GitlabProjectClient,
        serviceSecretOutcome: ServiceOneSecretOutcome?,
    ) {
        super.generateCode(
            recommenderRuntime,
            dcSettings,
            serviceRuntime,
            serviceConfig,
            gitlabProjectClient,
            serviceSecretOutcome,
        )
        codegenSupport.patchMetaConfig(recommenderRuntime, gitlabProjectClient)
    }

    override fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): Map<String, String> {
        return buildMap {
            putAll(super.resolveTemplateReplacements(recommenderRuntime, serviceRuntime, serviceConfig))
            put("\${DockerfileDragonflyFragment}", Log4jTemplateSupport.dragonflyDockerfileFragment())
            put("\${Log4jRecommendsAppenderBlock}", Log4jTemplateSupport.recommendsAppenderBlock())
            put("\${Log4jRecommendsLoggerBlock}", Log4jTemplateSupport.recommendsLoggerBlock())
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
            cloudCpuVcores = 16,
            cloudRamG = 16,
            cloudLanOut = "400M",
            cloudLanIn = "600M",
            cloudVolumeSize = "50g",
            cloudAvailability = "80%",
            cloudJavaXms = "8g",
            cloudJavaXmx = "8g",
            additionalEnv = recommenderEnvironment,
            additionalTestingEnv = recommenderEnvironment,
            extraPorts = listOf("3443", "40195"),
            maxTestingCloudPods = 1,
            productId = recommenderRuntime.productId
        )
    }
}
