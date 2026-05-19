package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.FlowProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.StageDefinitionProperties

@Component
class StageDefinitionRegistry(
    flowProperties: FlowProperties,
) {
    val stageDefinitions: List<StageDefinitionProperties> = flowProperties.stageDefinitions
    private val stageDefinitionsByName: Map<String, StageDefinitionProperties> = stageDefinitions.associateBy { definition ->
        definition.stageName
    }

    fun getStageDefinition(stageName: String): StageDefinitionProperties {
        return stageDefinitionsByName[stageName]
            ?: error("No stage definition found for stage '$stageName'")
    }
}
