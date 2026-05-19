package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.pipeline.PipelineOrchestrator
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.stage.StageOrchestrator
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task.TaskOrchestrator
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskWorker

@Service
class FlowEngineStartupCoordinator(
    private val appScope: CoroutineScope,
    private val taskOrchestrator: TaskOrchestrator,
    private val stageOrchestrator: StageOrchestrator,
    private val pipelineOrchestrator: PipelineOrchestrator,
    private val taskWorker: TaskWorker,
    private val taskRecoveryBarrier: TaskRecoveryBarrier,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        pipelineOrchestrator.startEventProcessing()
        stageOrchestrator.startEventProcessing()
        taskOrchestrator.startEventProcessing()
        taskWorker.startExecution()

        appScope.launch {
            try {
                taskOrchestrator.recoverAfterRestart()
                stageOrchestrator.recoverAfterRestart()
                pipelineOrchestrator.recoverAfterRestart()
            } finally {
                taskRecoveryBarrier.markRecoveryCompleted()
            }
        }
    }
}
