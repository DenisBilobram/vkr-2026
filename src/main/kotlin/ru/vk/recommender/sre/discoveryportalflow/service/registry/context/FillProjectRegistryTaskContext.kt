package ru.vk.recommender.sre.discoveryportalflow.service.registry.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ProjectRecommenderRuntime

@JsonIgnoreProperties(ignoreUnknown = true)
data class FillProjectRegistryTaskContext(
    val projectRecommender: ProjectRecommenderRuntime,
    val dcSettings: RecommenderDcSettings = RecommenderDcSettings(),
) : FlowTaskContext
