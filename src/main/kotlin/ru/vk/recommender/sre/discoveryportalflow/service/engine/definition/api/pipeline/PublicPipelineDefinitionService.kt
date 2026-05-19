package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.pipeline

import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineDefinitionInfo
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry.PipelineDefinitionRegistry

@Service
class PublicPipelineDefinitionService(
    private val pipelineDefinitionRegistry: PipelineDefinitionRegistry,
    providers: List<PublicPipelineDefinitionProvider>,
) {
    private val providersByPipelineName = providers.associateByUnique(PublicPipelineDefinitionProvider::pipelineName)

    fun getPublicPipelineNames(): List<String> {
        return publicPipelineDefinitions()
            .map { it.pipelineName.also { name -> requireProvider(name) } }
    }

    fun getPipelineDefinition(pipelineName: String): PipelineDefinitionInfo {
        requirePublic(pipelineName)
        return requireProvider(pipelineName).definition()
    }

    fun requirePublic(pipelineName: String) {
        val pipelineDefinition = pipelineDefinitionRegistry.getPipelineDefinition(pipelineName)
        require(pipelineDefinition.public) {
            "Pipeline '$pipelineName' is not public"
        }
    }

    private fun publicPipelineDefinitions() = pipelineDefinitionRegistry.pipelineDefinitions
        .filter { pipelineDefinition -> pipelineDefinition.public }

    private fun requireProvider(pipelineName: String): PublicPipelineDefinitionProvider {
        return providersByPipelineName[pipelineName]
            ?: error("No public contract configured for pipeline '$pipelineName'")
    }

    private fun <T> List<T>.associateByUnique(keySelector: (T) -> String): Map<String, T> {
        val grouped = groupBy(keySelector)
        val duplicates = grouped.filterValues { values -> values.size > 1 }.keys
        require(duplicates.isEmpty()) {
            "Multiple public pipeline contracts configured for: ${duplicates.joinToString(", ")}"
        }
        return grouped.mapValues { (_, values) -> values.single() }
    }
}
