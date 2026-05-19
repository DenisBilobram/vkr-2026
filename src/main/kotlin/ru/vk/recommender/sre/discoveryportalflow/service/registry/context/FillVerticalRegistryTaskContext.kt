package ru.vk.recommender.sre.discoveryportalflow.service.registry.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtCluster

@JsonIgnoreProperties(ignoreUnknown = true)
data class FillVerticalRegistryTaskContext(
    val recommender: RecommenderRuntime,
    val services: List<ServiceRuntime> = emptyList(),
    val dcSettings: RecommenderDcSettings = RecommenderDcSettings(),
    val teamsChatId: String? = null,
    val ytCluster: YtCluster = YtCluster.JUPITER,
) : FlowTaskContext
