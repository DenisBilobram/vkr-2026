package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties

data class PipelineDefinitionProperties(
    val pipelineName: String,
    val public: Boolean = false,
    val childrenType: PipelineChildrenType,
    val dependencyPipelines: List<String> = emptyList(),
    val pipelines: List<PipelineDefinitionProperties> = emptyList(),
    val stages: List<PipelineStageReferenceProperties> = emptyList(),
)
