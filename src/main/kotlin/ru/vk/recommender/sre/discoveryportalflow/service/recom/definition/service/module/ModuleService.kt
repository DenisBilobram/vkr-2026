package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.module

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import kotlin.reflect.KClass

abstract class ModuleService<TConfig : RecommenderServiceConfig>(
    type: ServiceType,
    serviceName: String,
    configClass: KClass<TConfig>,
    templateDirectory: String? = serviceName,
    sharedTemplateDirectories: List<String> = listOf("shared/module"),
    sourceDirectory: String? = null,
    gradleBuildCommand: String? = null,
    onecloudDirectoryName: String? = null,
    defaultScope: ServiceScope = ServiceScope.VERTICAL_SCOPED,
    hasTesting: Boolean = true,
    hasCanary: Boolean = true,
) : RecomService<TConfig>(
    type = type,
    serviceName = serviceName,
    configClass = configClass,
    templateDirectory = templateDirectory,
    sharedTemplateDirectories = sharedTemplateDirectories,
    sourceDirectory = sourceDirectory,
    gradleBuildCommand = gradleBuildCommand,
    onecloudDirectoryName = onecloudDirectoryName,
    defaultScope = defaultScope,
    hasTesting = hasTesting,
    hasCanary = hasCanary,
) {
    override fun generateCode(
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
        gitlabProjectClient: GitlabProjectClient,
        serviceSecretOutcome: ServiceOneSecretOutcome?,
    ) {
        codegenSupport.addProjectModule(serviceRuntime, gitlabProjectClient)
        codegenSupport.renderTemplateDirectories(
            templateDirectories = resolveGeneratedTemplateDirectories(),
            templateReplacements = resolveTemplateReplacements(
                recommenderRuntime = recommenderRuntime,
                serviceRuntime = serviceRuntime,
                serviceConfig = serviceConfig,
            ) + resolveOneSecretTemplateReplacements(
                serviceRuntime = serviceRuntime,
                serviceSecretOutcome = serviceSecretOutcome,
            ),
            gitlabProjectClient = gitlabProjectClient
        )
        codegenSupport.writeServiceInfo(
            recommenderRuntime,
            dcSettings,
            serviceRuntime,
            isPlatformService = false,
            gitlabProjectClient
        )
    }
}
