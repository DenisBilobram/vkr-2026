package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.context

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import java.nio.file.Path

@JsonIgnoreProperties(ignoreUnknown = true)
data class ServicehostTaskContext(
    val workspaceRoot: Path,
    val recommenderName: String,
    val serviceOwner: String,
    @JsonAlias("parentProductName")
    val projectName: String? = null,
    val services: List<ServiceRuntime> = emptyList(),
    val dcSettings: RecommenderDcSettings,
    @JsonAlias("serviceHostClusterName")
    val servicehostClusterName: String? = null,
) : FlowTaskContext
