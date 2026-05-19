package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.RecommenderServiceConfigDefaults
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
data class PlatformGatewayServiceConfig(
    override val type: ServiceType = ServiceType.PLATFORM_GATEWAY,
    override val serviceDisabled: Boolean = false,
    val baseShardsAmount: Int = RecommenderServiceConfigDefaults.BASE_SHARDS_COUNT,
    override val hasSnapshots: Boolean = false,
) : RecommenderServiceConfig(type, serviceDisabled) {

    init {
        require(baseShardsAmount >= 1) { "baseShardsAmount must be positive" }
    }
}
