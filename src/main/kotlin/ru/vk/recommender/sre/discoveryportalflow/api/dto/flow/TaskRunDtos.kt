package ru.vk.recommender.sre.discoveryportalflow.api.dto.flow

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.LogType
import java.time.Instant
import java.util.UUID

data class TaskRunInfo(
    val id: UUID,
    val taskName: String,
    val status: FlowStatus,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val attemptNumber: Int,
    val currentAttempt: Int,
    val totalAttempts: Int,
)

data class TaskDependencyInfo(
    val taskRunId: UUID,
    val dependencyTaskRunId: UUID,
    val taskName: String,
    val dependencyTaskName: String,
)

data class TaskLogInfo(
    val id: UUID,
    val status: FlowStatus?,
    val type: LogType,
    val message: String,
    val createdAt: Instant,
)
