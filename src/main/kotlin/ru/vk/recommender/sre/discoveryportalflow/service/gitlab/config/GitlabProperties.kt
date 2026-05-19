package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gitlab")
class GitlabProperties {
    var token: String? = null
    var repositoryProjectId: Int? = null
    var aiNamespaceId: Int? = null
    var aiNamespacePath: String = "ai"
    var gitlabUrl: String? = null
}
