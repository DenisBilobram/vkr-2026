package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabMergeRequest
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabMergeRequestDraft

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitlabMergeRequestTaskContext(
    var gitlabMergeRequestDraft: GitlabMergeRequestDraft? = null,
    var gitlabMergeRequestIid: Int? = null,
    var gitlabMergeRequestUrl: String? = null,
) : FlowTaskContext {

    fun prepareDraft(draft: GitlabMergeRequestDraft) {
        gitlabMergeRequestDraft = draft
        clearResolvedMergeRequest()
    }

    fun registerCreatedMergeRequest(mergeRequest: GitlabMergeRequest) {
        gitlabMergeRequestIid = mergeRequest.iid
        gitlabMergeRequestUrl = mergeRequest.webUrl
    }

    fun requireDraft(): GitlabMergeRequestDraft {
        return requireNotNull(gitlabMergeRequestDraft) {
            "GitLab merge request draft is required"
        }
    }

    fun requireMergeRequestIid(): Int {
        val draft = requireDraft()
        return requireNotNull(gitlabMergeRequestIid) {
            "GitLab merge request iid is required for ${draft.sourceBranch}"
        }
    }

    private fun clearResolvedMergeRequest() {
        gitlabMergeRequestIid = null
        gitlabMergeRequestUrl = null
    }
}
