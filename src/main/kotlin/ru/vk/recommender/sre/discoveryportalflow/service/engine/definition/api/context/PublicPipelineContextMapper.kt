package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.mapper.PipelineContextInputMapper
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.pipeline.PublicPipelineDefinitionService

@Service
class PublicPipelineContextMapper(
    private val publicPipelineDefinitionService: PublicPipelineDefinitionService,
    private val inputMappers: List<PipelineContextInputMapper>,
    private val objectMapper: ObjectMapper,
) {

    fun toPipelineContext(
        pipelineName: String,
        rawValues: JsonNode,
    ): ObjectNode {
        require(rawValues is ObjectNode) {
            "Raw pipeline values must be an object"
        }

        publicPipelineDefinitionService.requirePublic(pipelineName)
        val rawContext = RawPipelineContext(rawValues)
        val pipelineContext = objectMapper.createObjectNode()

        inputMappers.forEach { mapper -> mapper.map(rawContext, pipelineContext) }

        return pipelineContext
    }
}
