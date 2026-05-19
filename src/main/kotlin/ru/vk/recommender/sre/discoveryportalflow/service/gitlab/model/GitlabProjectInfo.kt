package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model

data class GitlabProjectInfo(
    val gitlabProjectName: String,
    val gitlabProjectPath: String,
    val gitlabRepositoryPath: String,
    var gitlabProjectId: Int? = null,
    var gitlabProjectUrl: String? = null,
)