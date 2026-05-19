package ru.vk.recommender.sre.discoveryportalflow.service.recom.context

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import java.nio.file.Path

@JsonIgnoreProperties(ignoreUnknown = true)
data class RuntimeTaskContext(
    val recommender: RecommenderRuntime,
    val services: List<ServiceRuntime> = emptyList(),
    val dcSettings: RecommenderDcSettings,
    val recommenderName: String = recommender.recommenderName,
    val recommenderClassName: String = recommender.names.displayName,
    val recommenderFolderName: String = recommender.names.folderName,
    @JsonAlias("parentProductName")
    val projectName: String? = recommender.projectName,
    val clusterName: String = recommender.clusterName,
    val workspaceRoot: Path = recommender.workspaceRoot,
    val serviceOwner: String = recommender.serviceOwner,
    val recomOneSecretId: String? = recommender.recomOneSecretId,
    val projectOneSecretId: String? = recommender.projectOneSecretId,
    @JsonAlias("serviceHostClusterName")
    val servicehostClusterName: String? = null,
    val branch: String? = null,
    val teamsChatId: String? = null,
) : FlowTaskContext
