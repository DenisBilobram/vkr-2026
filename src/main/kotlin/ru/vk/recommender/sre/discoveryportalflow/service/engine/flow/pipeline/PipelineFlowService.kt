package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.pipeline

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineRunEntity
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.creation.PipelineRunCreator

@Service
class PipelineFlowService(
    private val pipelineRunCreator: PipelineRunCreator,
) {

    fun createPipelineRun(pipelineName: String, pipelineContext: JsonNode): PipelineRunEntity {
        return pipelineRunCreator.createPipelineRun(pipelineName, pipelineContext)
    }
}
