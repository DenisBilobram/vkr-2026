package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability

import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

interface ServiceOnecloudCapability<TConfig : RecommenderServiceConfig> {
    val onecloudDirectoryName: String?
    val hasTesting: Boolean
    val hasCanary: Boolean

    fun resolveOnecloudDirectoryName(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String

    fun resolveQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings

    fun shouldGenerateOnecloudManifests(
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): Boolean = true

    fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): ServiceManifestSpec
}
