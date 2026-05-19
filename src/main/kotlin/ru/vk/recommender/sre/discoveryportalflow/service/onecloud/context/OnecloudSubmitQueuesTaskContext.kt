package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudSubmitQueuesTaskContext(
    val queueSubmissions: List<OnecloudQueueSubmission> = emptyList(),
) : FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudQueueSubmission(
    val queueJson: String = "",
    val user: Map<String, Any?> = emptyMap(),
    val dcs: List<DatacenterCode> = emptyList(),
)
