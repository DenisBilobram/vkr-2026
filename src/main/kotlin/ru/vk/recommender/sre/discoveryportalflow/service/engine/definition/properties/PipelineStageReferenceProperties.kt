package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties

data class PipelineStageReferenceProperties(
    val stageName: String,
    val bootstrapTask: String? = null,
    val dependencyStages: List<String> = emptyList(),
)
