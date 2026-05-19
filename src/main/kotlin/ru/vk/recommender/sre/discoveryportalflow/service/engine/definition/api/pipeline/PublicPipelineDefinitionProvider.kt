package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.pipeline

import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineDefinitionInfo

interface PublicPipelineDefinitionProvider {
    val pipelineName: String

    fun definition(): PipelineDefinitionInfo
}
