package ru.vk.recommender.sre.discoveryportalflow.service.recom.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.ProjectRecommenderConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig

@JsonIgnoreProperties(ignoreUnknown = true)
data class BootstrapProjectRecommenderContextTaskContext(
    val projectRecommender: ProjectRecommenderConfig,
    val services: List<RecommenderServiceConfig> = emptyList(),
    val dcSettings: RecommenderDcSettings = RecommenderDcSettings(),
    val servicehostClusterName: String? = null,
) : FlowTaskContext
