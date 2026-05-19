package ru.vk.recommender.sre.discoveryportalflow.service.toggles.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabCommitTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabMergeRequestTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabProjectTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabWebhookTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class TogglesTenantTaskContext(
    val tenantType: TogglesTenantType,
    val createTogglesTenantFlag: Boolean,
    val updateZeusRolesFlag: Boolean,
    val tenantName: String,
    val recommenderName: String,
    val owner: String,
    val dcs: List<String>,
    val abcIds: List<String>,
    val goldenSourceProjectId: Int,
    val replacements: Map<String, String> = emptyMap(),
    val gitlabProjectTaskContext: GitlabProjectTaskContext,
    val gitlabWebhookTaskContext: GitlabWebhookTaskContext,
    val gitlabCommitTaskContext: GitlabCommitTaskContext = GitlabCommitTaskContext(),
    val gitlabMergeRequestTaskContext: GitlabMergeRequestTaskContext = GitlabMergeRequestTaskContext(),
    var togglesConfigRequestIid: Int? = null,
    var togglesStoredVersion: String? = null,
) : FlowTaskContext

enum class TogglesTenantType {
    ONLINE,
    OFFLINE,
}
