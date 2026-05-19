package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import ru.vk.recommender.sre.discoveryportalflow.service.engine.logging.TasksRuntimeLogger
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.execution.TaskExecutionContextHolder
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class FlowTask<ContextType : FlowTaskContext>(
    val contextClass: KClass<ContextType>,
) {

    lateinit var runtimeLogger: TasksRuntimeLogger
    lateinit var taskExecutionContextHolder: TaskExecutionContextHolder

    suspend fun execute(taskRunContext: FlowTaskContext): TaskRunResult {
        if (!contextClass.isInstance(taskRunContext)) throw IllegalArgumentException("context must be instance of ${contextClass.simpleName}")
        val context = contextClass.cast(taskRunContext)
        return executeCasted(context)
    }

    fun updatedContext(taskRunContext: FlowTaskContext, objectMapper: ObjectMapper): JsonNode {
        if (!contextClass.isInstance(taskRunContext)) throw IllegalArgumentException("context must be instance of ${contextClass.simpleName}")
        val context = contextClass.cast(taskRunContext)
        return projectUpdatedContext(context, objectMapper)
    }

    protected abstract suspend fun executeCasted(taskRunContext: ContextType): TaskRunResult

    protected open fun projectUpdatedContext(taskRunContext: ContextType, objectMapper: ObjectMapper): JsonNode {
        return objectMapper.valueToTree(taskRunContext)
    }

    protected suspend fun currentTaskDefinition(): TaskDefinitionProperties {
        return taskExecutionContextHolder.current().taskDefinition
    }

    protected suspend fun currentTaskName(): String {
        return taskExecutionContextHolder.current().taskName
    }

    @Autowired
    fun setDependencies(
        runtimeLogger: TasksRuntimeLogger,
        taskExecutionContextHolder: TaskExecutionContextHolder
    ) {
        this.runtimeLogger = runtimeLogger
        this.taskExecutionContextHolder = taskExecutionContextHolder
    }

}
