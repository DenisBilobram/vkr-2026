package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.FlowProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties

@Component
class BootstrapTaskDefinitionRegistry(
    flowProperties: FlowProperties,
) {
    val bootstrapTaskDefinitions: List<TaskDefinitionProperties> = flowProperties.bootstrapTaskDefinitions
    private val bootstrapTaskDefinitionsByName: Map<String, TaskDefinitionProperties> = bootstrapTaskDefinitions.associateBy { definition ->
        definition.taskName
    }

    fun getBootstrapTaskDefinition(taskName: String): TaskDefinitionProperties {
        return bootstrapTaskDefinitionsByName[taskName]
            ?: error("No bootstrap task definition found for task '$taskName'")
    }
}
