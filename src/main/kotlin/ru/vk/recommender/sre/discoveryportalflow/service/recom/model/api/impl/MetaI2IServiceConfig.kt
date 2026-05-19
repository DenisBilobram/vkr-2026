package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
data class MetaI2IServiceConfig(
    override val type: ServiceType = ServiceType.META_I2I,
    override val serviceDisabled: Boolean = false,
    override val hasSnapshots: Boolean = true,
) : RecommenderServiceConfig(type, serviceDisabled)
