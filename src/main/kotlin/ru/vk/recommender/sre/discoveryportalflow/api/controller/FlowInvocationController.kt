package ru.vk.recommender.sre.discoveryportalflow.api.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRawContextTransformRequest
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunActionResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunRequest
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageRunActionResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.TaskRunActionResponse
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.PublicPipelineContextMapper
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.pipeline.PipelineFlowService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.stage.StageFlowService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task.TaskRetryService
import java.util.UUID

@RestController
@RequestMapping("/flow")
class FlowInvocationController(
    private val pipelineFlowService: PipelineFlowService,
    private val publicPipelineContextMapper: PublicPipelineContextMapper,
    private val stageFlowService: StageFlowService,
    private val taskRetryService: TaskRetryService,
) {

    @PostMapping("pipeline/create")
    fun createPipeline(@Valid @RequestBody pipelineRunRequest: PipelineRunRequest): PipelineRunActionResponse {
        val pipelineRun = pipelineFlowService.createPipelineRun(
            pipelineRunRequest.pipelineName,
            pipelineRunRequest.pipelineContext,
        )
        return PipelineRunActionResponse(
            pipelineRunId = pipelineRun.requireId(),
            pipelineName = pipelineRun.pipelineName,
        )
    }

    @PostMapping("pipeline/create-with-transform")
    fun createPipelineWithTransform(
        @Valid @RequestBody request: PipelineRawContextTransformRequest,
    ): PipelineRunActionResponse {
        val pipelineContext = publicPipelineContextMapper.toPipelineContext(
            pipelineName = request.pipelineName,
            rawValues = request.rawContext,
        )
        val pipelineRun = pipelineFlowService.createPipelineRun(request.pipelineName, pipelineContext)
        return PipelineRunActionResponse(
            pipelineRunId = pipelineRun.requireId(),
            pipelineName = pipelineRun.pipelineName,
        )
    }

    @PostMapping("stage/{stageRunId}/start")
    fun startStage(@PathVariable stageRunId: UUID): StageRunActionResponse {
        val stageRun = stageFlowService.startStage(stageRunId)
        return StageRunActionResponse(
            stageRunId = stageRun.requireId(),
            pipelineName = stageRun.requirePipelineName(),
            stageName = stageRun.requireStageName(),
            pipelineRunId = stageRun.requirePipelineRunId(),
        )
    }

    @PostMapping("task/{taskRunId}/retry")
    fun retryTask(@PathVariable taskRunId: UUID): TaskRunActionResponse {
        val taskRun = taskRetryService.retryTask(taskRunId)
        return TaskRunActionResponse(
            taskRunId = taskRun.requireId(),
            stageRunId = taskRun.stageRunId,
            taskName = taskRun.taskName,
            attemptNumber = taskRun.attemptNumber,
        )
    }

}
