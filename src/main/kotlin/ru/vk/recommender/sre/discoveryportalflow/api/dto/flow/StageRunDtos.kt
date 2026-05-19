package ru.vk.recommender.sre.discoveryportalflow.api.dto.flow

import com.fasterxml.jackson.databind.JsonNode
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import java.time.Instant
import java.util.UUID

data class StageRunDetailsResponse(
    val stageRun: StageRunInfo,
    val parentPipelineRunId: UUID,
    val parentPipelineName: String,
    val stats: StageRunStats,
    val tasks: List<TaskRunInfo>,
    val dependencies: List<TaskDependencyInfo>,
)

data class StageRunWithTasksInfo(
    val stageRun: StageRunInfo,
    val stats: StageRunStats,
    val tasks: List<TaskRunInfo>,
    val dependencies: List<TaskDependencyInfo>,
)

data class StageRunInfo(
    val id: UUID,
    val pipelineRunId: UUID,
    val pipelineName: String,
    val stageName: String,
    val status: FlowStatus,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val summary: JsonNode,
)

data class StageRunStats(
    val total: Int,
    val pending: Int,
    val ready: Int,
    val running: Int,
    val waiting: Int,
    val blocked: Int,
    val succeeded: Int,
    val failed: Int,
    val skipped: Int,
    val canceled: Int,
)

data class StageDependencyInfo(
    val stageRunId: UUID,
    val dependencyStageRunId: UUID,
    val stageName: String,
    val dependencyStageName: String,
)
