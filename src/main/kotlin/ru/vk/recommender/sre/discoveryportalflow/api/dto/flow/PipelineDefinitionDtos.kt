package ru.vk.recommender.sre.discoveryportalflow.api.dto.flow

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PipelineDefinitionInfo(
    val pipelineName: String,
    val label: String,
    val tabs: List<PipelineContextTabInfo>,
)
