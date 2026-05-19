package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.pipeline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.PipelineRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineChildrenType
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.FlowChannels
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.PipelineStatusChangedEvent
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.stage.StageStatusService
import java.util.UUID

@Service
class PipelineOrchestrator(
    private val channels: FlowChannels,
    private val appScope: CoroutineScope,
    private val pipelineStatusService: PipelineStatusService,
    private val stageStatusService: StageStatusService,
    private val pipelineRuntimeService: PipelineRuntimeService,
    private val stageRuntimeService: StageRuntimeService,
) {

    fun startEventProcessing() {
        appScope.launch { processPipelineEvents() }
    }

    fun recomputePipelineSubtree(pipelineRunId: UUID) {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(pipelineRunId)
        if (pipelineRun.status in pipelineRuntimeService.terminalStatuses) {
            return
        }

        val updatedPipelineRun = pipelineStatusService.recomputePipelineStatus(pipelineRunId)
        if (updatedPipelineRun.status !in activationStatuses) {
            return
        }

        when (updatedPipelineRun.childrenType) {
            PipelineChildrenType.PIPELINE -> {
                pipelineRuntimeService.getChildPipelineRuns(pipelineRunId)
                    .forEach { childPipelineRun -> recomputePipelineSubtree(childPipelineRun.requireId()) }
            }

            PipelineChildrenType.STAGE -> {
                stageRuntimeService.getStageRunsForPipelineRun(pipelineRunId)
                    .forEach { stageRun ->
                        stageStatusService.recomputeStageStatus(stageRun.requireId())
                    }
            }
        }

        pipelineStatusService.recomputePipelineStatus(pipelineRunId)
    }

    private suspend fun processPipelineEvents() {
        for (event in channels.pipelineStatusChangedChannel) {
            handlePipelineStatusChanged(event)
        }
    }

    private fun handlePipelineStatusChanged(event: PipelineStatusChangedEvent) {
        if (event.newStatus in pipelineRuntimeService.dependencySatisfiedStatuses) {
            pipelineRuntimeService.getDependents(event.pipelineRunId)
                .forEach { dependency ->
                    recomputePipelineSubtree(dependency.pipelineRunId)
                }
        }

        event.parentPipelineRunId?.let(pipelineStatusService::recomputePipelineStatus)
    }

    fun recoverAfterRestart() {
        pipelineRuntimeService.getAllPipelineRuns()
            .filter { pipelineRun -> pipelineRun.parentPipelineRunId == null }
            .sortedBy { pipelineRun -> pipelineRun.createdAt }
            .forEach { rootPipelineRun -> recomputePipelineSubtree(rootPipelineRun.requireId()) }
    }

    private companion object {
        val activationStatuses = setOf(
            FlowStatus.READY,
            FlowStatus.RUNNING,
        )
    }
}
