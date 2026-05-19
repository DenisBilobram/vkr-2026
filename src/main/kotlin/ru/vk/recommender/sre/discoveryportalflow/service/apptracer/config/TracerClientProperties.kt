package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tracer-client")
data class TracerClientProperties(
    val url: String = "",
    val connectTimeoutSeconds: Long = 3,
    val requestTimeoutSeconds: Long = 10,
    val serviceRefreshToken: String = "",
)
