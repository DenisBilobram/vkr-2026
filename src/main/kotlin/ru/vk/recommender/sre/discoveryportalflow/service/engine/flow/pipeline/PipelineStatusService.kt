package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.pipeline

import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.PipelineRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineChildrenType
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.FlowStatusEventPublisher
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.PipelineStatusChangedEvent

@Service
class PipelineStatusService(
    private val pipelineRuntimeService: PipelineRuntimeService,
    private val stageRuntimeService: StageRuntimeService,
    private val flowStatusEventPublisher: FlowStatusEventPublisher,
) {

    fun updatePipelineStatus(pipelineRun: PipelineRunEntity, newStatus: FlowStatus): PipelineRunEntity {
        if (pipelineRun.status == newStatus) {
            return pipelineRun
        }
        val updatedPipelineRun = pipelineRuntimeService.setPipelineRunStatus(pipelineRun, newStatus)
        flowStatusEventPublisher.publishPipelineStatusChanged(
            PipelineStatusChangedEvent(
                pipelineRunId = updatedPipelineRun.requireId(),
                parentPipelineRunId = updatedPipelineRun.parentPipelineRunId,
                oldStatus = pipelineRun.status,
                newStatus = updatedPipelineRun.status,
            )
        )
        return updatedPipelineRun
    }

    fun recomputePipelineStatus(pipelineRunId: java.util.UUID): PipelineRunEntity {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(pipelineRunId)
        return updatePipelineStatus(pipelineRun, resolvePipelineStatus(pipelineRun))
    }

    private fun resolvePipelineStatus(pipelineRun: PipelineRunEntity): FlowStatus {
        val pipelineRunId = pipelineRun.requireId()
        val childStatuses = when (pipelineRun.childrenType) {
            PipelineChildrenType.PIPELINE -> pipelineRuntimeService.getChildPipelineRuns(pipelineRunId)
                .map { childPipelineRun -> childPipelineRun.status }

            PipelineChildrenType.STAGE -> stageRuntimeService.getStageRunsForPipelineRun(pipelineRunId)
                .map { stageRun -> stageRun.status }
        }

        if (childStatuses.isEmpty()) {
            return FlowStatus.SUCCEEDED
        }

        return when {
            childStatuses.any { status -> status == FlowStatus.BLOCKED } -> FlowStatus.BLOCKED
            childStatuses.any { status -> status == FlowStatus.FAILED } -> FlowStatus.FAILED
            childStatuses.any { status -> status == FlowStatus.CANCELED } -> FlowStatus.CANCELED
            childStatuses.any { status -> status == FlowStatus.RUNNING || status == FlowStatus.WAITING } -> FlowStatus.RUNNING
            childStatuses.all { status -> status == FlowStatus.SUCCEEDED || status == FlowStatus.SKIPPED } -> FlowStatus.SUCCEEDED
            childStatuses.any { status -> status == FlowStatus.READY } -> FlowStatus.READY
            pipelineCanBecomeReady(pipelineRun) -> FlowStatus.READY
            else -> FlowStatus.PENDING
        }
    }

    private fun pipelineCanBecomeReady(pipelineRun: PipelineRunEntity): Boolean {
        return pipelineRuntimeService.allPipelineDependenciesSatisfied(pipelineRun.requireId()) &&
            parentPipelineAllowsActivation(pipelineRun)
    }

    private fun parentPipelineAllowsActivation(pipelineRun: PipelineRunEntity): Boolean {
        val parentPipelineRunId = pipelineRun.parentPipelineRunId ?: return true
        val parentPipelineRun = pipelineRuntimeService.getPipelineRun(parentPipelineRunId)
        return parentPipelineRun.status in parentReadyStatuses
    }

    private companion object {
        val parentReadyStatuses = setOf(
            FlowStatus.READY,
            FlowStatus.RUNNING,
        )
    }
}
