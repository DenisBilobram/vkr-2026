package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "servicehost.admin")
class ServicehostAdminProperties {
    var baseUrl: String = "https://servicehost.nda.example.invalid/api/v1/projects"
    var project: String = "public"
    var requestTimeoutMs: Long = 30000
}
