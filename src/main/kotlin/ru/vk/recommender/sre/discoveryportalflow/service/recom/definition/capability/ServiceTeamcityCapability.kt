package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability

import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime

interface ServiceTeamcityCapability<TConfig : RecommenderServiceConfig> {
    fun resolveTeamcityProject(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String
}
