package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.FlowProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineStageReferenceProperties

@Component
class PipelineDefinitionRegistry(
    flowProperties: FlowProperties,
) {
    val rootPipelineDefinitions: List<PipelineDefinitionProperties> = flowProperties.pipelineDefinitions
    val pipelineDefinitions: List<PipelineDefinitionProperties> = flatten(rootPipelineDefinitions)
    private val pipelineDefinitionsByName: Map<String, PipelineDefinitionProperties> = pipelineDefinitions.associateBy { definition ->
        definition.pipelineName
    }

    fun getPipelineDefinition(pipelineName: String): PipelineDefinitionProperties {
        return pipelineDefinitionsByName[pipelineName]
            ?: error("No pipeline definition found for name $pipelineName")
    }

    fun getPipelineStageReference(
        pipelineName: String,
        stageName: String,
    ): PipelineStageReferenceProperties {
        val pipelineDefinition = getPipelineDefinition(pipelineName)
        return pipelineDefinition.stages.firstOrNull { stageReference ->
            stageReference.stageName == stageName
        } ?: error("No stage reference '$stageName' found in pipeline '$pipelineName'")
    }

    private fun flatten(definitions: List<PipelineDefinitionProperties>): List<PipelineDefinitionProperties> {
        return definitions.flatMap { definition ->
            listOf(definition) + flatten(definition.pipelines)
        }
    }
}
