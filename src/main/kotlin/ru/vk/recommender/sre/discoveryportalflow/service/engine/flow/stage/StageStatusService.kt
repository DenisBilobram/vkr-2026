package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.stage

import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.PipelineRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.TaskRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.FlowStatusEventPublisher
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.StageStatusChangedEvent
import java.util.UUID

@Service
class StageStatusService(
    private val stageRuntimeService: StageRuntimeService,
    private val taskRuntimeService: TaskRuntimeService,
    private val pipelineRuntimeService: PipelineRuntimeService,
    private val flowStatusEventPublisher: FlowStatusEventPublisher,
) {

    fun updateStageStatus(stageRun: StageRunEntity, newStatus: FlowStatus): StageRunEntity {
        if (stageRun.status == newStatus) {
            return stageRun
        }
        val updatedStageRun = stageRuntimeService.setStageRunStatus(stageRun, newStatus)
        flowStatusEventPublisher.publishStageStatusChanged(
            StageStatusChangedEvent(
                stageRunId = updatedStageRun.requireId(),
                pipelineRunId = updatedStageRun.requirePipelineRunId(),
                oldStatus = stageRun.status,
                newStatus = updatedStageRun.status,
            )
        )
        return updatedStageRun
    }

    fun recomputeStageStatus(stageRunId: UUID): StageRunEntity {
        val stageRun = stageRuntimeService.getStageRun(stageRunId)
        val taskRuns = taskRuntimeService.getTaskRunsForStageRunId(stageRunId)
        return updateStageStatus(stageRun, resolveStageStatus(stageRun, taskRuns))
    }

    private fun resolveStageStatus(
        stageRun: StageRunEntity,
        taskRuns: List<TaskRunEntity>,
    ): FlowStatus {
        if (stageRun.status in stageRuntimeService.terminalStatuses) {
            return stageRun.status
        }

        return when (stageRun.status) {
            FlowStatus.PENDING -> if (stageCanBecomeReady(stageRun)) FlowStatus.READY else FlowStatus.PENDING
            FlowStatus.READY -> FlowStatus.READY
            FlowStatus.RUNNING,
            FlowStatus.WAITING,
            FlowStatus.BLOCKED -> resolveActiveStageStatus(taskRuns)
            else -> stageRun.status
        }
    }

    private fun stageCanBecomeReady(stageRun: StageRunEntity): Boolean {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(stageRun.requirePipelineRunId())
        return pipelineRun.status in parentReadyStatuses &&
            stageRuntimeService.allStageDependenciesSatisfied(stageRun.requireId())
    }

    private fun resolveActiveStageStatus(taskRuns: List<TaskRunEntity>): FlowStatus {
        if (taskRuns.isEmpty()) {
            return FlowStatus.SUCCEEDED
        }

        return when {
            taskRuns.any { taskRun -> taskRun.status == FlowStatus.FAILED } -> FlowStatus.BLOCKED
            taskRuns.any { taskRun -> taskRun.status == FlowStatus.WAITING } -> FlowStatus.WAITING
            taskRuns.all { taskRun -> taskRun.status == FlowStatus.SUCCEEDED || taskRun.status == FlowStatus.SKIPPED } -> FlowStatus.SUCCEEDED
            taskRuns.any { taskRun ->
                taskRun.status == FlowStatus.RUNNING || taskRun.status == FlowStatus.READY ||
                taskRun.status == FlowStatus.PENDING || taskRun.status == FlowStatus.FAILED_WITH_RETRY
            } -> FlowStatus.RUNNING
            taskRuns.any { taskRun -> taskRun.status == FlowStatus.CANCELED } -> FlowStatus.CANCELED
            else -> FlowStatus.PENDING
        }
    }

    private companion object {
        val parentReadyStatuses = setOf(
            FlowStatus.READY,
            FlowStatus.RUNNING,
        )
    }
}
