package ru.vk.recommender.sre.discoveryportalflow.service.vkteams.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderConfig

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateVkTeamsChatContext(
    val recommender: RecommenderConfig,
    val teamsUsersToAdd: List<String>,
    var teamsChatId: String? = null,
) : FlowTaskContext
