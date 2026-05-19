package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration

import org.springframework.stereotype.Component

@Component
class FlowStatusEventPublisher(
    private val channels: FlowChannels,
) {

    fun publishTaskStatusChanged(event: TaskStatusChangedEvent) {
        val result = channels.taskStatusChangedChannel.trySend(event)
        check(result.isSuccess) { "Failed to publish task status event for taskRunId=${event.taskRunId}" }
    }

    fun publishStageStatusChanged(event: StageStatusChangedEvent) {
        val result = channels.stageStatusChangedChannel.trySend(event)
        check(result.isSuccess) { "Failed to publish stage status event for stageRunId=${event.stageRunId}" }
    }

    fun publishPipelineStatusChanged(event: PipelineStatusChangedEvent) {
        val result = channels.pipelineStatusChangedChannel.trySend(event)
        check(result.isSuccess) { "Failed to publish pipeline status event for pipelineRunId=${event.pipelineRunId}" }
    }
}
