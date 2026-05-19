package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry.StageUsageResolver

@Component
class TaskDefinitionResolver(
    private val stageRuntimeService: StageRuntimeService,
    private val stageUsageResolver: StageUsageResolver,
) {

    fun getTaskDefinition(taskRun: TaskRunEntity): TaskDefinitionProperties {
        val stageRun = stageRuntimeService.getStageRun(taskRun.stageRunId)
        return stageUsageResolver.getTaskDefinitions(
            pipelineName = stageRun.requirePipelineName(),
            stageName = stageRun.requireStageName(),
        ).firstOrNull { definition -> definition.taskName == taskRun.taskName }
            ?: error(
                "No task definition found for pipeline '${stageRun.requirePipelineName()}', stage '${stageRun.requireStageName()}' and task '${taskRun.taskName}'"
            )
    }
}
