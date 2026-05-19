package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.execution

import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import java.time.Instant
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class TaskExecutionContext(
    val taskRunId: UUID,
    val taskName: String,
    val taskBeanName: String,
    val taskDefinition: TaskDefinitionProperties,
    val stageRunId: UUID,
    val attemptNumber: Int,
    val startedAt: Instant,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<TaskExecutionContext>
}
