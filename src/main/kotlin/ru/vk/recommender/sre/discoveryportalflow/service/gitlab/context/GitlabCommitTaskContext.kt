package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabCommitDraft

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitlabCommitTaskContext(
    var gitlabCommitDraft: GitlabCommitDraft? = null,
) : FlowTaskContext {

    fun prepareDraft(draft: GitlabCommitDraft) {
        gitlabCommitDraft = draft
    }

    fun requireDraft(): GitlabCommitDraft {
        return requireNotNull(gitlabCommitDraft) {
            "GitLab commit draft is required"
        }
    }
}
