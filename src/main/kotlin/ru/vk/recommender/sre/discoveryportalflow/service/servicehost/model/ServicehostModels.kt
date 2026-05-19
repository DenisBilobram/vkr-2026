package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import java.nio.charset.StandardCharsets
import java.util.Base64

data class ServicehostGraphNames(
    val exportGraphName: String,
    val recommendGraphName: String,
    val recommendBaseGraphName: String,
    val storagesGraphName: String,
    val offlineGraphName: String,
    val offlineRecommendGraphName: String,
    val offlineBaseGraphName: String,
)

data class ServicehostClusterNames(
    val normalizedClusterName: String,
    val fullClusterName: String,
)

data class ServicehostEncodedEntry(
    val name: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val type: String?,
    var content: String,
)

object Mesh {
    val prodShMesh = listOf("servicehost/mesh", "servicehost-new/mesh")
    val canaryShMesh = listOf("canary-servicehost/mesh", "canary-servicehost-new/mesh")
    val testingShMesh =
        listOf(
            "testing-servicehost/mesh",
            "testing-servicehost-new/mesh",
            "stage-servicehost/mesh",
            "stage-servicehost-new/mesh"
        )
    val allMesh = (prodShMesh + canaryShMesh + testingShMesh).toSet()
}

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class ServicehostRequestPayload(
    @JsonProperty("graphs")
    private val graphs: MutableList<ServicehostEncodedEntry> = mutableListOf(),
    @JsonProperty("backends")
    private val backends: MutableList<ServicehostEncodedEntry> = mutableListOf(),
    @JsonProperty("resources")
    private val resources: MutableList<ServicehostEncodedEntry> = mutableListOf(),
) {
    companion object {

        fun encodeBase64(content: String): String =
            Base64.getEncoder().encodeToString(content.toByteArray(StandardCharsets.UTF_8))

        fun decodeBase64(content: String): String =
            String(Base64.getDecoder().decode(content), Charsets.UTF_8)
    }

    fun addBackend(name: String, content: String, environment: ServiceEnvironment) {
        this.backends.add(
            ServicehostEncodedEntry(
                name = "$name-mesh",
                type = ServicehostResourceType.BACKEND.type,
                content = encodeBase64(content),
            )
        )

        this.backends.filter {
            it.type == ServicehostResourceType.CLUSTER_MESH.type
        }.map {
            val decodedContent = decodeBase64(it.content)

            if (
                (it.name in Mesh.prodShMesh && environment == ServiceEnvironment.PRODUCTION) ||
                (it.name in Mesh.canaryShMesh && environment == ServiceEnvironment.CANARY) ||
                (it.name in Mesh.testingShMesh && environment == ServiceEnvironment.TESTING)
            ) {
                val updatedContent =
                    MeshServiceUpdater.addServiceToUpstreams(fileContent = decodedContent, serviceName = name)
                it.content = encodeBase64(updatedContent)
            }
        }
    }

    fun addGraph(name: String, content: String) {
        this.graphs.add(
            ServicehostEncodedEntry(
                name = name,
                type = ServicehostResourceType.GRAPH.type,
                content = encodeBase64(content),
            )
        )
        if (name.startsWith("export-")) {
            this.graphs.filter {
                it.name == "routes" && it.type == null
            }.map {
                val decodedContent = decodeBase64(it.content)
                val updatedContent = GraphRoutesUpdater.addRoute(name, decodedContent)
                it.content = encodeBase64(updatedContent)
            }
        }
    }
}

data class ServicehostBackendTestsPayload(
    val components: MutableMap<String, ServicehostBackendTestComponent> = mutableMapOf(),
)

data class ServicehostBackendTestComponent(
    val teamcityJobName: String,
)

data class ServicehostBackendConfig(
    val backendName: String,
    val onecloudShardName: String,
    val port: Int = 24816,
    val rootPath: String = "/",
)

data class ServicehostGraphNodeConfig(
    val nodeName: String,
    val nodeDependencies: List<String>,
    val backendName: String,
    val handler: String,
    val hardTimeout: String,
    val softTimeout: String? = null,
    val retryOn: String? = null,
    val maxReaskBudget: String? = null,
    val requestsPerReask: String? = null,
    val teamcityProject: String? = null,
    val forceRequestOnEmptyInput: Boolean? = null,
    val responsibles: List<String>? = null,
    val codecs: List<String> = listOf("lz4"),
    val neverDiscard: Boolean? = null,
)

data class ServicehostGraphShardedNodeConfig(
    val shardCount: Int,
    val backendName: String,
    val nodeDependencies: List<String>,
    val handler: String,
    val hardTimeout: String,
    val softTimeout: String? = null,
    val retryOn: String? = null,
    val nodeName: String = "SHARD",
    val codecs: List<String> = listOf("lz4"),
    val teamcityProject: String? = null,
)

data class ServicehostGraphTransparentNodeConfig(
    val nodeName: String,
    val nodeDependencies: List<String>,
)

data class ServicehostGraphEmbedNodeConfig(
    val nodeName: String,
    val params: List<Map<String, String>> = emptyList(),
)

data class ServicehostGraphConfig(
    val graphName: String,
    val graphInputDeps: List<String>,
    val graphOutputDeps: Map<String, List<String>>,
    val responsibles: List<String>,
    val edgeExpressions: Map<String, String> = emptyMap(),
    val graphNodes: List<ServicehostGraphNodeConfig> = emptyList(),
    val graphTransparentNodes: List<ServicehostGraphTransparentNodeConfig> = emptyList(),
    val graphEmbedNodes: List<ServicehostGraphEmbedNodeConfig> = emptyList(),
    val graphShardedNodes: List<ServicehostGraphShardedNodeConfig> = emptyList(),
    val allowEmptyResponse: String? = null,
)

data class ServicehostClusterInitializationResult(
    val fullClusterName: String,
    val createdNow: Boolean,
    val clusterMrUrl: String?,
)

data class ServicehostConfigApplyResult(
    val fullClusterName: String,
    val configMrUrl: String?,
)

internal fun RecommenderDcSettings.uniqueDatacenterCodes(): List<String> {
    return (productionDcs + canaryDcs + testingDcs).distinct().sorted()
}

data class ServicehostResourceResponse(
    val items: List<ServicehostResourceItem>
)

data class ServicehostResourceItem(
    val name: String,
    val type: String
) {
    fun typeEquals(type: ServicehostResourceType): Boolean {
        return this.type == type.type
    }
}

data class ServicehostFile(
    val name: String,
    val path: String,
    var content: String
)

enum class ServicehostResourceType(val type: String) {
    CLUSTER_MESH("sh-mesh"),
    BACKEND("backend-mesh"),
    GRAPH("graph"),
    ROUTE("route")
}

data class ServicehostClusterResponse(
    val items: List<ServicehostClusterItem>
)

data class ServicehostClusterItem(
    val name: String,
    @JsonProperty("creation_status")
    val creationStatus: String?
)
