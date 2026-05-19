package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.StageDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties

@Component
class StageUsageResolver(
    private val pipelineDefinitionRegistry: PipelineDefinitionRegistry,
    private val stageDefinitionRegistry: StageDefinitionRegistry,
    private val bootstrapTaskDefinitionRegistry: BootstrapTaskDefinitionRegistry,
) {

    fun getStageDefinition(
        pipelineName: String,
        stageName: String,
    ): StageDefinitionProperties {
        val stageReference = getStageReference(pipelineName, stageName)
        return stageDefinitionRegistry.getStageDefinition(stageReference.stageName)
    }

    fun getTaskDefinitions(
        pipelineName: String,
        stageName: String,
    ): List<TaskDefinitionProperties> {
        val stageReference = getStageReference(pipelineName, stageName)
        val stageDefinition = stageDefinitionRegistry.getStageDefinition(stageReference.stageName)
        val bootstrapTaskDefinition = stageReference.bootstrapTask?.let(bootstrapTaskDefinitionRegistry::getBootstrapTaskDefinition)

        val bootstrapTask = bootstrapTaskDefinition?.copy(
            disabled = false,
            dependencyTasks = emptyList(),
        )
        val stageTasks = stageDefinition.enabledTaskDefinitions().map { taskDefinition ->
            if (bootstrapTask == null || taskDefinition.dependencyTasks.isNotEmpty()) {
                taskDefinition
            } else {
                taskDefinition.copy(dependencyTasks = listOf(bootstrapTask.taskName))
            }
        }

        return listOfNotNull(bootstrapTask) + stageTasks
    }

    private fun getStageReference(
        pipelineName: String,
        stageName: String,
    ) = pipelineDefinitionRegistry.getPipelineStageReference(
        pipelineName = pipelineName,
        stageName = stageName,
    )
}
