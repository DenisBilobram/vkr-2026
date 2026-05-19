package ru.vk.recommender.sre.discoveryportalflow.api.dto.flow

import com.fasterxml.jackson.databind.JsonNode
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineChildrenType
import java.time.Instant
import java.util.UUID

data class PipelineRunsPageResponse(
    val page: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int,
    val pipelineRuns: List<PipelineRunInfo>,
)

data class PipelineRunDetailsResponse(
    val pipelineRun: PipelineRunInfo,
    val parentPipelineName: String?,
    val childPipelines: List<PipelineRunInfo>,
    val pipelineDependencies: List<PipelineDependencyInfo>,
    val stages: List<StageRunInfo>,
    val stageDependencies: List<StageDependencyInfo>,
)

data class PipelineStageRunsDetailsResponse(
    val pipelineRun: PipelineRunInfo,
    val stages: List<StageRunWithTasksInfo>,
    val stageDependencies: List<StageDependencyInfo>,
)

data class PipelineRunInfo(
    val id: UUID,
    val pipelineName: String,
    val childrenType: PipelineChildrenType,
    val parentPipelineRunId: UUID?,
    val status: FlowStatus,
    val totalChildren: Int,
    val completedChildren: Int,
    val createdAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val summary: JsonNode,
)

data class PipelineDependencyInfo(
    val pipelineRunId: UUID,
    val dependencyPipelineRunId: UUID,
    val pipelineName: String,
    val dependencyPipelineName: String,
)
