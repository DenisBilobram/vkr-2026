package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.creation

import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.TaskRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry.StageUsageResolver
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.validation.FlowRunValidator
import java.util.UUID

@Service
class TaskRunCreator(
    private val taskRuntimeService: TaskRuntimeService,
    private val stageUsageResolver: StageUsageResolver,
    private val flowRunValidator: FlowRunValidator,
) {

    fun createTaskRuns(stageRun: StageRunEntity): Map<String, TaskRunEntity> {
        val taskDefinitions = stageUsageResolver.getTaskDefinitions(
            pipelineName = stageRun.requirePipelineName(),
            stageName = stageRun.requireStageName(),
        )
        return createTaskGraph(stageRun, taskDefinitions)
    }

    private fun createTaskGraph(
        stageRun: StageRunEntity,
        taskDefinitions: List<TaskDefinitionProperties>,
    ): Map<String, TaskRunEntity> {
        flowRunValidator.requireTaskRunsNotCreated(
            stageRun = stageRun,
            existingTaskRunsCount = taskRuntimeService.getTaskRunsForStageRun(stageRun).size,
        )
        val taskRunsByName = taskDefinitions.associate { definition ->
            definition.taskName to taskRuntimeService.createTaskRun(stageRun, definition.taskName)
        }
        val dependencies = buildTaskRunDependencies(taskDefinitions, taskRunsByName)
        taskRuntimeService.saveTaskRunDependencies(dependencies)
        return taskRunsByName
    }

    private fun buildTaskRunDependencies(
        taskDefinitions: List<TaskDefinitionProperties>,
        taskRunsByName: Map<String, TaskRunEntity>
    ): List<TaskDependencyEntity> {
        fun taskRunIdByName(taskName: String): UUID = taskRunsByName.getValue(taskName).requireId()

        return taskDefinitions.flatMap { definition ->
            val taskRunId = taskRunIdByName(definition.taskName)
            definition.dependencyTasks.distinct().map { dependencyTaskName ->
                TaskDependencyEntity(taskRunId = taskRunId, dependencyTaskRunId = taskRunIdByName(dependencyTaskName))
            }
        }
    }
}
