package ru.vk.recommender.sre.discoveryportalflow.service.mdb.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mdb")
data class MdbProperties(
    val baseUrl: String,
    val token: String,
    val clusterId: String,
    val userRole: String,
    val connectTimeoutSeconds: Long,
    val requestTimeoutSeconds: Long,
    val operationPollDelayMillis: Long,
    val operationTimeoutSeconds: Long,
) {
    init {
        require(baseUrl.isNotBlank()) { "mdb.base-url must not be blank" }
        require(clusterId.isNotBlank()) { "mdb.cluster-id must not be blank" }
        require(userRole.isNotBlank()) { "mdb.user-role must not be blank" }
        require(connectTimeoutSeconds > 0) { "mdb.connect-timeout-seconds must be > 0" }
        require(requestTimeoutSeconds > 0) { "mdb.request-timeout-seconds must be > 0" }
        require(operationPollDelayMillis > 0) { "mdb.operation-poll-delay-millis must be > 0" }
        require(operationTimeoutSeconds > 0) { "mdb.operation-timeout-seconds must be > 0" }
    }
}
