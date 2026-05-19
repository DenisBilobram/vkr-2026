package ru.vk.recommender.sre.discoveryportalflow.api.controller

import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineDefinitionInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunsPageResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineStageRunsDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageRunDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.TaskLogInfo
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.FlowQueryService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.pipeline.PublicPipelineDefinitionService
import java.util.UUID

@RestController
@RequestMapping("/flow")
class FlowQueryController(
    private val flowQueryService: FlowQueryService,
    private val publicPipelineDefinitionService: PublicPipelineDefinitionService,
) {

    @GetMapping("pipelines")
    suspend fun getPipelineRuns(
        @RequestParam(defaultValue = "1") page: Int,
    ): PipelineRunsPageResponse {
        return flowQueryService.getPipelineRuns(page)
    }

    @GetMapping("pipeline/{pipelineRunId}")
    suspend fun getPipelineRunDetails(@PathVariable pipelineRunId: UUID): PipelineRunDetailsResponse {
        return flowQueryService.getPipelineRunDetails(pipelineRunId)
    }

    @GetMapping("pipeline/{pipelineRunId}/context")
    suspend fun getPipelineContext(@PathVariable pipelineRunId: UUID): ObjectNode {
        return flowQueryService.getPipelineContext(pipelineRunId)
    }

    @PutMapping("pipeline/{pipelineRunId}/context")
    suspend fun updatePipelineContext(
        @PathVariable pipelineRunId: UUID,
        @RequestBody pipelineContext: ObjectNode,
    ): ObjectNode {
        return flowQueryService.updatePipelineContext(pipelineRunId, pipelineContext)
    }

    @GetMapping("pipeline/{pipelineRunId}/stages/details")
    suspend fun getPipelineStageRunsDetails(@PathVariable pipelineRunId: UUID): PipelineStageRunsDetailsResponse {
        return flowQueryService.getPipelineStageRunsDetails(pipelineRunId)
    }

    @GetMapping("stage/{stageRunId}")
    suspend fun getStageRunDetails(@PathVariable stageRunId: UUID): StageRunDetailsResponse {
        return flowQueryService.getStageRunDetails(stageRunId)
    }

    @GetMapping("task/{taskRunId}/logs")
    suspend fun getTaskLogs(@PathVariable taskRunId: UUID): List<TaskLogInfo> {
        return flowQueryService.getTaskLogs(taskRunId)
    }

    @GetMapping("definitions/pipelines")
    suspend fun getPublicPipelineNames(): List<String> {
        return publicPipelineDefinitionService.getPublicPipelineNames()
    }

    @GetMapping("definitions/pipeline/{pipelineName}")
    suspend fun getPipelineDefinition(@PathVariable pipelineName: String): PipelineDefinitionInfo {
        return publicPipelineDefinitionService.getPipelineDefinition(pipelineName)
    }
}
