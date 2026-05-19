package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties

import java.time.Duration

data class WaitingPolicyProperties(
    val waitingTask: Boolean = false,
    val delay: Duration = Duration.ZERO,
)
