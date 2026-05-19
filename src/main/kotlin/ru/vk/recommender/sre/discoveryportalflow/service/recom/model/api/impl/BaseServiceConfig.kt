package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.RecommenderServiceConfigDefaults
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ShardedServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
data class BaseServiceConfig(
    override val type: ServiceType = ServiceType.BASE,
    override val serviceDisabled: Boolean = false,
    override val shardsCount: Int = RecommenderServiceConfigDefaults.BASE_SHARDS_COUNT,
    override val hasSnapshots: Boolean = true,
) : RecommenderServiceConfig(type, serviceDisabled), ShardedServiceConfig {

    init {
        require(shardsCount >= 1) { "shardsCount must be positive" }
    }
}
