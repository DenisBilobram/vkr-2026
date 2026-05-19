package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig

data class MediatorServiceConfig(
    override val type: ServiceType = ServiceType.MEDIATOR,
    override val serviceDisabled: Boolean = false,
    val withCache: Boolean = false,
) : RecommenderServiceConfig(type, serviceDisabled)
