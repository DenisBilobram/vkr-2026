package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudSubmitServicesTaskContext(
    val serviceSubmissions: List<OnecloudServiceSubmission> = emptyList(),
) : FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudServiceSubmission(
    val serviceJson: String = "",
    val queue: String? = null,
    val replicas: String? = null,
    val minRunning: String? = null,
    val pause: String? = null,
    val dcs: List<DatacenterCode> = emptyList(),
)
