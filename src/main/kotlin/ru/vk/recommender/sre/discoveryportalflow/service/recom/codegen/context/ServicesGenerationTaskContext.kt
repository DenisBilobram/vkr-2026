package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabCommitTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabMergeRequestTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

@JsonIgnoreProperties(ignoreUnknown = true)
data class ServicesGenerationTaskContext(
    val recommender: RecommenderRuntime,
    val services: List<ServiceRuntime> = emptyList(),
    val dcSettings: RecommenderDcSettings,
    val serviceOneSecretOutcomes: Map<String, ServiceOneSecretOutcome> = emptyMap(),
    val gitlabCommitTaskContext: GitlabCommitTaskContext = GitlabCommitTaskContext(),
    val gitlabMergeRequestTaskContext: GitlabMergeRequestTaskContext = GitlabMergeRequestTaskContext(),
    val fishJiraTask: String
) : FlowTaskContext
