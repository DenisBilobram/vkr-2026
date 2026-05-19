package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.stage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.TaskRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.FlowChannels
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.StageStatusChangedEvent
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.TaskRecoveryBarrier
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.pipeline.PipelineStatusService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task.TaskStatusService

@Service
class StageOrchestrator(
    private val channels: FlowChannels,
    private val appScope: CoroutineScope,
    private val stageStatusService: StageStatusService,
    private val taskStatusService: TaskStatusService,
    private val pipelineStatusService: PipelineStatusService,
    private val stageRuntimeService: StageRuntimeService,
    private val taskRuntimeService: TaskRuntimeService,
    private val taskRecoveryBarrier: TaskRecoveryBarrier,
) {

    fun startEventProcessing() {
        appScope.launch { processStageEvents() }
    }

    private suspend fun processStageEvents() {
        for (event in channels.stageStatusChangedChannel) {
            handleStageStatusChanged(event)
        }
    }

    private fun handleStageStatusChanged(event: StageStatusChangedEvent) {
        when (event.newStatus) {
            FlowStatus.RUNNING -> {
                recomputeStageTasks(event.stageRunId)
                pipelineStatusService.recomputePipelineStatus(event.pipelineRunId)
            }

            FlowStatus.SUCCEEDED,
            FlowStatus.SKIPPED -> {
                recomputeDependentStages(event.stageRunId)
                pipelineStatusService.recomputePipelineStatus(event.pipelineRunId)
            }

            FlowStatus.WAITING,
            FlowStatus.BLOCKED,
            FlowStatus.CANCELED -> {
                pipelineStatusService.recomputePipelineStatus(event.pipelineRunId)
            }

            else -> Unit
        }
    }

    private fun recomputeStageTasks(stageRunId: java.util.UUID) {
        appScope.launch {
            taskRecoveryBarrier.awaitRecoveryCompleted()
            taskRuntimeService.getTaskRunsForStageRunId(stageRunId)
                .forEach { taskRun ->
                    taskStatusService.recomputeTaskStatus(taskRun.requireId())
                }
            stageStatusService.recomputeStageStatus(stageRunId)
        }
    }

    private fun recomputeDependentStages(stageRunId: java.util.UUID) {
        stageRuntimeService.getDependents(stageRunId)
            .forEach { dependency ->
                stageStatusService.recomputeStageStatus(dependency.stageRunId)
            }
    }

    fun recoverAfterRestart() {
        stageRuntimeService.getAllStageRuns()
            .filter { stageRun -> stageRun.status in recoveredStatuses }
            .forEach { stageRun ->
                stageStatusService.recomputeStageStatus(stageRun.requireId())
            }
    }

    private companion object {
        val recoveredStatuses = setOf(
            FlowStatus.RUNNING,
            FlowStatus.WAITING,
            FlowStatus.BLOCKED,
        )
    }
}
