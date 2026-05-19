package ru.vk.recommender.sre.discoveryportalflow.service.ocs.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings

@JsonIgnoreProperties(ignoreUnknown = true)
data class OcsTaskContext(
    val recommenderName: String,
    val serviceOwner: String,
    val dcSettings: RecommenderDcSettings,
) : FlowTaskContext
