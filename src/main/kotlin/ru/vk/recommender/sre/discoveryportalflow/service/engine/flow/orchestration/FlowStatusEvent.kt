package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import java.util.UUID

sealed interface FlowStatusChangedEvent {
    val oldStatus: FlowStatus
    val newStatus: FlowStatus
}

data class TaskStatusChangedEvent(
    val taskRunId: UUID,
    val stageRunId: UUID,
    override val oldStatus: FlowStatus,
    override val newStatus: FlowStatus,
) : FlowStatusChangedEvent

data class StageStatusChangedEvent(
    val stageRunId: UUID,
    val pipelineRunId: UUID,
    override val oldStatus: FlowStatus,
    override val newStatus: FlowStatus,
) : FlowStatusChangedEvent

data class PipelineStatusChangedEvent(
    val pipelineRunId: UUID,
    val parentPipelineRunId: UUID?,
    override val oldStatus: FlowStatus,
    override val newStatus: FlowStatus,
) : FlowStatusChangedEvent
