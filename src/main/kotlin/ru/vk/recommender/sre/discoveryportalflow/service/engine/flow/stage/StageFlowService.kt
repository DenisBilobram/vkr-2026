package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.stage

import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.TaskRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry.StageUsageResolver
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context.ContextFlagEvaluator
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context.ContextRunDecision
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context.FlowContextManager
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task.TaskStatusService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.validation.FlowRunValidator
import java.util.UUID

@Service
class StageFlowService(
    private val stageRuntimeService: StageRuntimeService,
    private val taskRuntimeService: TaskRuntimeService,
    private val stageUsageResolver: StageUsageResolver,
    private val flowContextManager: FlowContextManager,
    private val contextFlagEvaluator: ContextFlagEvaluator,
    private val flowRunValidator: FlowRunValidator,
    private val stageStatusService: StageStatusService,
    private val taskStatusService: TaskStatusService,
) {

    fun startStage(stageRunId: UUID): StageRunEntity {
        val stageRun = stageRuntimeService.getStageRun(stageRunId)
        flowRunValidator.requireStageCanStart(stageRun)
        val stageContext = flowContextManager.loadFlowContextByStageRunId(stageRunId)
        val taskRuns = taskRuntimeService.getTaskRunsForStageRun(stageRun)
        val stageDefinition = resolveStageDefinition(stageRun)
        return when (
            contextFlagEvaluator.resolveRunDecision(
                subjectDescription = "Stage '${stageDefinition.stageName}'",
                executeIfPath = stageDefinition.executeIf,
                contextJson = stageContext,
            )
        ) {
            ContextRunDecision.Run -> stageStatusService.updateStageStatus(stageRun, FlowStatus.RUNNING)
            is ContextRunDecision.Skip -> skipStageRun(stageRun, taskRuns)
        }
    }

    private fun resolveStageDefinition(stageRun: StageRunEntity) =
        stageUsageResolver.getStageDefinition(
            pipelineName = stageRun.requirePipelineName(),
            stageName = stageRun.requireStageName(),
        )

    private fun skipStageRun(
        stageRun: StageRunEntity,
        taskRuns: Collection<TaskRunEntity>,
    ): StageRunEntity {
        val skippedStageRun = stageStatusService.updateStageStatus(stageRun, FlowStatus.SKIPPED)
        taskRuns.forEach { taskRun ->
            taskStatusService.updateTaskStatus(taskRun, FlowStatus.SKIPPED)
        }
        return skippedStageRun
    }
}
