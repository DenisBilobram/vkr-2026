package ru.vk.recommender.sre.discoveryportalflow.service.pms.context

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import java.nio.file.Path

@JsonIgnoreProperties(ignoreUnknown = true)
data class ServicePmsTaskContext(
    val recommenderName: String,
    val recommenderFolderName: String,
    @JsonAlias("parentProductName")
    val projectName: String? = null,
    val clusterName: String,
    val workspaceRoot: Path,
    val recomOneSecretId: String? = null,
    val projectOneSecretId: String? = null,
    val services: List<ServiceRuntime> = emptyList(),
) : FlowTaskContext
