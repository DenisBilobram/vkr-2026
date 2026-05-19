package ru.vk.recommender.sre.discoveryportalflow.api.dto.flow

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class PipelineRunRequest(
    @field:NotBlank
    val pipelineName: String,
    val pipelineContext: JsonNode,
)

data class PipelineRawContextTransformRequest(
    @field:NotBlank
    val pipelineName: String,
    @JsonAlias("values")
    val rawContext: JsonNode,
)

data class PipelineRunActionResponse(
    val pipelineRunId: UUID,
    val pipelineName: String,
)

data class StageRunActionResponse(
    val stageRunId: UUID,
    val pipelineName: String,
    val stageName: String,
    val pipelineRunId: UUID,
)

data class TaskRunActionResponse(
    val taskRunId: UUID,
    val stageRunId: UUID,
    val taskName: String,
    val attemptNumber: Int,
)
