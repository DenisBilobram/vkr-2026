package ru.vk.recommender.sre.discoveryportalflow.service.yt.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtCluster

@JsonIgnoreProperties(ignoreUnknown = true)
data class YtMockTablesTaskContext(
    val projectName: String,
    val recommenderName: String,
    val yt: YtRuntimeCredentials,
    val ytCluster: YtCluster,
    val rootPath: String = DEFAULT_MOCK_TABLES_ROOT_PATH,
    val itemTypeId: Long = DEFAULT_ITEM_TYPE_ID,
    val embeddingVersion: Int = DEFAULT_EMBEDDING_VERSION,
) : FlowTaskContext {
    companion object {
        const val DEFAULT_MOCK_TABLES_ROOT_PATH = "//tmp/recommender-flow"
        const val DEFAULT_ITEM_TYPE_ID = 1L
        const val DEFAULT_EMBEDDING_VERSION = 1
    }
}
