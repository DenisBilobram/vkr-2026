package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabWebhookInfo

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitlabWebhookTaskContext(
    val gitlabWebhookInfo: GitlabWebhookInfo,
) : FlowTaskContext
