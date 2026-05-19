package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties

import java.time.Duration

data class TaskDefinitionProperties(
    val taskName: String,
    val taskBean: String,
    val executeIf: String? = null,
    val timeout: Duration = Duration.ofSeconds(60),
    val dependencyTasks: List<String> = emptyList(),
    val disabled: Boolean = false,
    val skipOnFailed: Boolean = false,
    val retryPolicy: RetryPolicyProperties = RetryPolicyProperties(),
    val waitingPolicy: WaitingPolicyProperties = WaitingPolicyProperties(),
)
