package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.utils

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabMergeRequestDraft

internal fun GitlabMergeRequestDraft.toGitlabReference(
    iid: Int? = null,
): String {
    return buildString {
        append("projectId=").append(projectId)
        append(", sourceBranch=").append(sourceBranch)
        append(", targetBranch=").append(targetBranch)
        iid?.let { append(", iid=!").append(it) }
    }
}
