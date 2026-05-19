package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

interface ServiceCodegenCapability<TConfig : RecommenderServiceConfig> {
    val templateDirectory: String?
    val sharedTemplateDirectories: List<String>
    val sourceDirectory: String?
    val gradleBuildCommand: String?

    fun generateCode(
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
        gitlabProjectClient: GitlabProjectClient,
        serviceSecretOutcome: ServiceOneSecretOutcome? = null,
    )

    fun resolveSourceDirectory(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String

    fun resolveGradleBuildCommand(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String

    fun resolveGeneratedTemplateDirectories(): List<String>

    fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): Map<String, String>
}
