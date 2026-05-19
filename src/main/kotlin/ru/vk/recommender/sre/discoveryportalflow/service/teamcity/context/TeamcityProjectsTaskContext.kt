package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.context

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.TriggeredBuild

@JsonIgnoreProperties(ignoreUnknown = true)
data class TeamcityProjectsTaskContext(
    val recommenderName: String,
    val recommenderClassName: String,
    @JsonAlias("parentProductName")
    val projectName: String? = null,
    val services: List<ServiceRuntime> = emptyList(),
    val dcSettings: RecommenderDcSettings,
    val branch: String?,
    val teamsChatId: String,
    val builds: MutableSet<TriggeredBuild> = mutableSetOf(),
) : FlowTaskContext
