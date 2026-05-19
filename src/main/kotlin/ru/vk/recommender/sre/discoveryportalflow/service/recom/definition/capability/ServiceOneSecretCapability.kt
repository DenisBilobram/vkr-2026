package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

interface ServiceOneSecretCapability<TConfig : RecommenderServiceConfig> {
    val hasSecret: Boolean
    val allowEmptySecretData: Boolean

    fun buildSecretPairs(
        taskContext: OneSecretTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): Map<String, String>
}
