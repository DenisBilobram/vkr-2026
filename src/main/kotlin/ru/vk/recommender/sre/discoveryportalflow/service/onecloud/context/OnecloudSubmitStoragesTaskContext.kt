package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudSubmitStoragesTaskContext(
    val storageSubmissions: List<OnecloudStorageSubmission> = emptyList(),
) : FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudStorageSubmission(
    val storageJson: String = "",
    val queue: String? = null,
    val shards: Int? = null,
    val dcs: List<DatacenterCode> = emptyList(),
)
