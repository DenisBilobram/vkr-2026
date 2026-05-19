package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability

import ru.vk.recommender.sre.discoveryportalflow.service.pms.context.ServicePmsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.pms.service.submit.ServicePmsSubmitServiceOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServicePmsDefinition
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

interface ServicePmsCapability<TConfig : RecommenderServiceConfig> {
    val pms: ServicePmsDefinition

    fun submitPmsConfig(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): ServicePmsSubmitServiceOutcome
}
