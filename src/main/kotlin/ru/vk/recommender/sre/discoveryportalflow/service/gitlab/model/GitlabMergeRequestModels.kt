package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model

data class GitlabProject(
    val id: Int,
    val name: String? = null,
    val path: String? = null,
    val pathWithNamespace: String? = null,
    val webUrl: String? = null,
)

data class GitlabCommitDraft(
    val projectId: Int,
    val branch: String,
    val commitMessage: String,
    val files: List<GitlabFile>,
    val startBranch: String = "master",
)

data class GitlabMergeRequestDraft(
    val projectId: Int,
    val sourceBranch: String,
    val title: String,
    val targetBranch: String = "master",
    val squash: Boolean = true,
)

data class GitlabMergeRequest(
    val iid: Int,
    val webUrl: String? = null,
)

data class GitlabPipeline(
    val id: Int,
    val status: String,
)
