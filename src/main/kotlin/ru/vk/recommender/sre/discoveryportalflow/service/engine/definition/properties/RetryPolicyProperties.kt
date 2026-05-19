package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties

import java.time.Duration

data class RetryPolicyProperties(
    val autoRetryEnabled: Boolean = false,
    val backoff: Duration = Duration.ofSeconds(60),
    val attempts: Int = 2,
)
