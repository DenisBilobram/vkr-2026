package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ScopedServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
data class SnapshotsBuilderServiceConfig(
    override val type: ServiceType = ServiceType.SNAPSHOTS_BUILDER,
    override val serviceDisabled: Boolean = false,
    override val serviceScope: ServiceScope = ServiceScope.PROJECT_SCOPED,
) : RecommenderServiceConfig(type, serviceDisabled), ScopedServiceConfig
