package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context.ContextFlagEvaluator
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context.ContextRunDecision
import ru.vk.recommender.sre.discoveryportalflow.service.engine.logging.TasksRuntimeLogger
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.exception.RetryableException
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.execution.TaskExecutionContextHolder

@Component
class TaskRunner(
    private val tasks: Map<String, FlowTask<out FlowTaskContext>>,
    private val objectMapper: ObjectMapper,
    private val taskExecutionContextHolder: TaskExecutionContextHolder,
    private val contextFlagEvaluator: ContextFlagEvaluator,
    private val tasksRuntimeLogger: TasksRuntimeLogger,
) {
    suspend fun run(
        taskRun: TaskRunEntity,
        taskDefinition: TaskDefinitionProperties,
        contextJson: JsonNode,
    ): TaskExecutionResult {
        val taskBeanName = taskDefinition.taskBean
        val task = tasks[taskBeanName] ?: error("No task bean found for name: $taskBeanName")
        val taskContextJson = resolveTaskContextJson(contextJson, task)
        val taskContext = objectMapper.treeToValue(taskContextJson.contextJson, task.contextClass.java)

        return taskExecutionContextHolder.withTaskRun(
            taskRun = taskRun,
            taskBeanName = taskBeanName,
            taskDefinition = taskDefinition,
        ) {
            when (
                val executionDecision = contextFlagEvaluator.resolveRunDecision(
                    subjectDescription = "Task '${taskDefinition.taskName}'",
                    executeIfPath = taskDefinition.executeIf,
                    contextJson = contextJson,
                )
            ) {
                ContextRunDecision.Run -> Unit
                is ContextRunDecision.Skip -> {
                    tasksRuntimeLogger.info(
                        "Skipped task '${taskDefinition.taskName}': execute-if flag '${executionDecision.flagPath}' evaluated to false"
                    )
                    return@withTaskRun TaskExecutionResult(
                        taskStatus = FlowStatus.SKIPPED,
                        updatedContext = objectMapper.createObjectNode(),
                    )
                }
            }

            supervisorScope {
                val taskExecution = async {
                    withTimeout(taskDefinition.timeout.toMillis()) {
                        task.execute(taskContext)
                    }
                }
                val result = try {
                    taskExecution.await()
                } catch (timeoutException: TimeoutCancellationException) {
                    throw RetryableException("Task timeout", timeoutException)
                }
                return@supervisorScope TaskExecutionResult(
                    taskStatus = result.taskStatus,
                    updatedContext = wrapUpdatedContext(
                        updatedContext = task.updatedContext(taskContext, objectMapper),
                        taskContextJson = taskContextJson,
                    ),
                )
            }
        }
    }

    private fun resolveTaskContextJson(
        contextJson: JsonNode,
        task: FlowTask<out FlowTaskContext>,
    ): ResolvedTaskContextJson {
        if (contextJson !is ObjectNode) {
            return ResolvedTaskContextJson(contextJson = contextJson)
        }

        val nestedFieldName = task.contextClass.simpleName
            ?.replaceFirstChar { firstChar -> firstChar.lowercase() }
            ?.takeIf { contextJson.has(it) }

        return if (nestedFieldName == null) {
            ResolvedTaskContextJson(contextJson = contextJson)
        } else {
            ResolvedTaskContextJson(
                contextJson = contextJson.get(nestedFieldName),
                nestedFieldName = nestedFieldName,
            )
        }
    }

    private fun wrapUpdatedContext(
        updatedContext: JsonNode,
        taskContextJson: ResolvedTaskContextJson,
    ): JsonNode {
        val nestedFieldName = taskContextJson.nestedFieldName ?: return updatedContext
        return objectMapper.createObjectNode().set<JsonNode>(nestedFieldName, updatedContext)
    }

    private data class ResolvedTaskContextJson(
        val contextJson: JsonNode,
        val nestedFieldName: String? = null,
    )
}
