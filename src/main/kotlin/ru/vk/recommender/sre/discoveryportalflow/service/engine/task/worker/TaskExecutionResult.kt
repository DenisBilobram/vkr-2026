package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker

import com.fasterxml.jackson.databind.JsonNode
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus

data class TaskExecutionResult(
    val taskStatus: FlowStatus,
    val updatedContext: JsonNode,
)
