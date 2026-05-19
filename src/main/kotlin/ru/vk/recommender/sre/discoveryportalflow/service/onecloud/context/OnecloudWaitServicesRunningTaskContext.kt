package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudWaitServicesRunningTaskContext(
    val serviceTargets: List<OnecloudServiceWaitTarget> = emptyList(),
) : FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudServiceWaitTarget(
    val name: String = "",
    val dcs: List<DatacenterCode> = emptyList(),
)
