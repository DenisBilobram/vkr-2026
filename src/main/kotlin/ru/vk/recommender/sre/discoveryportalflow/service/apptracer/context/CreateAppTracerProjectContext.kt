package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderConfig

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateAppTracerProjectContext(
    val recommender: RecommenderConfig,
    val orgId: Long = DEFAULT_ORG_ID,
    var apptracerToken: String? = null,
) : FlowTaskContext {
    private companion object {
        private const val DEFAULT_ORG_ID = 3_478_748L
    }
}
