package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
data class SelectorsServiceConfig(
    override val type: ServiceType = ServiceType.SELECTORS,
    override val serviceDisabled: Boolean = false,
    override val hasSnapshots: Boolean = false,
) : RecommenderServiceConfig(type, serviceDisabled)
