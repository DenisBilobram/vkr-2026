package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "flow")
data class FlowProperties(
    val pipelineDefinitions: List<PipelineDefinitionProperties> = emptyList(),
    val stageDefinitions: List<StageDefinitionProperties> = emptyList(),
    val bootstrapTaskDefinitions: List<TaskDefinitionProperties> = emptyList(),
)
