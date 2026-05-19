package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.Backend
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.BackendItem
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.Graph
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.GraphItem
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.Node
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostRequestPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostResourceType


/**
 * @author mikh.nikiforov
 */
class ServicehostNodesParser(val client: ServicehostAdminClient) {

    fun getNodes(): MutableMap<String, MutableList<Node>> {
        val result = mutableMapOf<String, MutableList<Node>>()
        val clusters = client.getClusters()

        cluster@ for (cluster in clusters) {
            val backends = mutableMapOf<String, MutableList<Backend>>()
            client.getBackends(cluster).items
                .filter {
                    it.typeEquals(ServicehostResourceType.BACKEND)
                            && !it.name.startsWith("canary-")
                            && !it.name.startsWith("testing")
                }
                .forEach {
                    val backend = client.getBackend(cluster, it.name)
                    val backendContent = ServicehostRequestPayload.decodeBase64(backend.content)
                    val meshConfigs = parseMeshServiceConfigs(backendContent)
                    meshConfigs.forEach { config ->
                        val backendKey =
                            config.name!!.replace("-", "_").replace("shard\\d+".toRegex(), "{{SHARD}}").uppercase()
                        backends.getOrPut(backendKey) { mutableListOf() }.add(config)
                    }
                }

            val graphs = client.getGraphs(cluster)
            graph@ for (graph in graphs.items) {
                if (!graph.typeEquals(ServicehostResourceType.GRAPH) || graph.name == "export-vk-stories") {
                    continue@graph
                }
                val graphFile = client.getGraph(cluster, graph.name)
                val graphContent = ServicehostRequestPayload.decodeBase64(graphFile.content)
                val graphNodes = parseGraphNodeResources(graphContent)
                println("Found ${graphNodes.size} GraphNode resources in cluster $cluster, graph ${graph.name}")
                val nodes = mutableListOf<Node>()
                node@ for (node in graphNodes) {
                    if (!backends.containsKey(node.backend)) {
                        continue@node
                    }
                    backends[node.backend]!!.forEach { backend ->
                        nodes.add(Node(name = node.name, backend = backend.cloudServiceName!!, handler = node.handler))
                    }
                }
                result.getOrPut(graph.name) { mutableListOf() }.addAll(nodes)
            }
        }
        return result
    }

    private fun parseGraphNodeResources(yamlContent: String): List<Graph> {
        val yamlDocuments = yamlContent.split("---\\n".toRegex())
            .filter { it.trim().isNotEmpty() }

        val objectMapper = ObjectMapper(YAMLFactory())
        val graphNodes = mutableListOf<Graph>()

        for (document in yamlDocuments) {
            try {
                val item = objectMapper.readValue(document.trim(), GraphItem::class.java)
                if ((item.kind == "GraphNode" || item.kind == "GraphNodeSharded")
                    && item.metadata?.nodeName != null
                    && item.spec?.backend != null
                    && item.spec.backend != "SELF"
                ) {
                    val graph = Graph(
                        name = item.metadata.nodeName,
                        backend = item.spec.backend,
                        handler = item.spec.params?.get("handler").toString()
                    )
                    graphNodes.add(graph)
                }
            } catch (e: Exception) {
                // Skip invalid documents
                println("Skipping invalid YAML document: ${e.message}")
            }
        }

        return graphNodes
    }

    /**
     * Parses a multi-document YAML string, splits it by nodes using "---" separator,
     * and filters for resources with kind: MeshServiceConfig
     */
    private fun parseMeshServiceConfigs(yamlContent: String): List<Backend> {
        val yamlDocuments = yamlContent.split("---\\n".toRegex())
            .filter { it.trim().isNotEmpty() }

        val objectMapper = ObjectMapper(YAMLFactory())
        val meshConfigs = mutableListOf<Backend>()

        for (document in yamlDocuments) {
            try {
                val item = objectMapper.readValue(document.trim(), BackendItem::class.java)
                if (item.kind == "MeshServiceConfig" && item.metadata?.name != null) {
                    val config = Backend(
                        name = item.metadata.name,
                        cloudServiceName = item.metadata.cloudServiceName
                    )
                    meshConfigs.add(config)
                }
            } catch (e: Exception) {
                // Skip invalid documents
                println("Skipping invalid YAML document: ${e.message}")
            }
        }

        return meshConfigs
    }
}
