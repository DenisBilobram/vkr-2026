package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
data class WorkerServiceConfig(
    override val type: ServiceType = ServiceType.WORKER,
    override val serviceDisabled: Boolean = false,
    val defaultPool: String? = null,
) : RecommenderServiceConfig(type, serviceDisabled)
