package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model

data class GitlabWebhookInfo(
    val gitlabRepositoryPath: String,
    val gitlabWebhookUrl: String,
)

