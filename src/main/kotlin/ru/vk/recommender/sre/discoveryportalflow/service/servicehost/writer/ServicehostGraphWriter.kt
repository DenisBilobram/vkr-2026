package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer

import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.writeText
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostBackendTestComponent
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostBackendTestsPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphNodeConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphShardedNodeConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostRequestPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.renderer.ServicehostYamlRawDumper
import java.nio.file.Path

class ServicehostGraphWriter {

    fun writeGraphConfigs(
        servicehostRootDirectory: Path,
        recommenderName: String,
        graphConfigs: List<ServicehostGraphConfig>,
        servicehostRequestPayload: ServicehostRequestPayload,
        backendTestsPayload: ServicehostBackendTestsPayload,
    ) {
        val graphOutputDirectory = servicehostRootDirectory
            .resolve("dsl/public/public-$recommenderName-prod/gateway/graphs")

        graphConfigs.forEach { graphConfig ->
            val graphContent = buildGraphYamlContent(graphConfig, backendTestsPayload)
            writeText(
                graphOutputDirectory.resolve("${graphConfig.graphName}.yaml"),
                graphContent,
            )
            servicehostRequestPayload.addGraph(graphConfig.graphName, graphContent)
        }
    }

    private fun buildGraphYamlContent(
        graphConfig: ServicehostGraphConfig,
        backendTestsPayload: ServicehostBackendTestsPayload,
    ): String {
        val documentsByName = linkedMapOf<String, MutableMap<String, Any?>>()

        graphConfig.graphShardedNodes.forEach { shardedNode ->
            documentsByName[shardedNode.nodeName] = linkedMapOf()
        }
        graphConfig.graphNodes.forEach { node ->
            documentsByName[node.nodeName] = linkedMapOf()
        }
        graphConfig.graphEmbedNodes.forEach { node ->
            documentsByName[node.nodeName] = linkedMapOf()
        }
        graphConfig.graphTransparentNodes.forEach { node ->
            documentsByName[node.nodeName] = linkedMapOf()
        }

        val mainNodeDeps = linkedMapOf<String, Any?>()

        val mainGraphSpec = linkedMapOf<String, Any?>().apply {
            graphConfig.allowEmptyResponse?.let { allowEmpty ->
                put("allow_empty_response", allowEmpty)
            }
            if (graphConfig.edgeExpressions.isNotEmpty()) {
                put(
                    "edge_expressions",
                    linkedMapOf<String, Any?>().apply {
                        graphConfig.edgeExpressions.forEach { (edge, expression) ->
                            put(edge, quote(expression))
                        }
                    },
                )
            }
            put("input_deps", graphConfig.graphInputDeps)
            put("output_deps", graphConfig.graphOutputDeps.keys.toList())
            put("node_deps", mainNodeDeps)
            put(
                "responsibles",
                linkedMapOf(
                    "logins" to graphConfig.responsibles.map { responsible -> quote(responsible) },
                ),
            )
        }

        documentsByName["main_graph"] = linkedMapOf(
            "apiVersion" to "v1",
            "kind" to "ClusterGraph",
            "metadata" to linkedMapOf(
                "graph_name" to quote(graphConfig.graphName),
            ),
            "spec" to mainGraphSpec,
        )

        graphConfig.graphNodes.forEach { graphNode ->
            mainNodeDeps[graphNode.nodeName] = graphNode.nodeDependencies.map { dependency -> quote(dependency) }
            documentsByName[graphNode.nodeName] = buildGraphNodeDocument(graphNode, backendTestsPayload)
        }

        graphConfig.graphShardedNodes.forEach { shardedNode ->
            mainNodeDeps[shardedNode.nodeName] = shardedNode.nodeDependencies.map { dependency -> quote(dependency) }
            documentsByName[shardedNode.nodeName] = buildGraphShardedNodeDocument(shardedNode, backendTestsPayload)
        }

        graphConfig.graphTransparentNodes.forEach { transparentNode ->
            mainNodeDeps[transparentNode.nodeName] = transparentNode.nodeDependencies.map { dependency -> quote(dependency) }
            documentsByName[transparentNode.nodeName] = linkedMapOf(
                "apiVersion" to "v1",
                "kind" to "GraphNode",
                "metadata" to linkedMapOf(
                    "node_name" to quote(transparentNode.nodeName),
                ),
                "spec" to linkedMapOf(
                    "nodeType" to "'TRANSPARENT'",
                ),
            )
        }

        graphConfig.graphEmbedNodes.forEach { embedNode ->
            documentsByName[embedNode.nodeName] = linkedMapOf(
                "apiVersion" to "v1",
                "kind" to "GraphNode",
                "metadata" to linkedMapOf(
                    "node_name" to embedNode.nodeName,
                ),
                "spec" to linkedMapOf(
                    "nodeType" to "'EMBED'",
                    "embed" to embedNode.params,
                ),
            )
        }

        graphConfig.graphOutputDeps.forEach { (nodeName, dependencies) ->
            mainNodeDeps[nodeName] = dependencies.map { dependency -> quote(dependency) }
        }

        val documents = documentsByName.values.toList()
        return ServicehostYamlRawDumper.dumpDocuments(documents)
    }

    private fun buildGraphNodeDocument(
        graphNode: ServicehostGraphNodeConfig,
        backendTestsPayload: ServicehostBackendTestsPayload,
    ): MutableMap<String, Any?> {
        if (graphNode.teamcityProject != null && graphNode.backendName != SELF_BACKEND_NAME) {
            backendTestsPayload.components[graphNode.backendName] = ServicehostBackendTestComponent(
                teamcityJobName = "${graphNode.teamcityProject}_GitlabCITestApphostGraphs",
            )
        }

        val nodeParams = linkedMapOf<String, Any?>().apply {
            if (graphNode.backendName != SELF_BACKEND_NAME) {
                put("codecs", graphNode.codecs)
            }
            put("handler", quote(graphNode.handler))
            graphNode.retryOn?.let { retryOn ->
                put("retry_on", quote(retryOn))
            }
            graphNode.softTimeout?.let { softTimeout ->
                put("soft_timeout", quote(softTimeout))
            }
            put("timeout", quote(graphNode.hardTimeout))
            if (graphNode.maxReaskBudget != null || graphNode.requestsPerReask != null) {
                val loadControlConfig = linkedMapOf<String, Any?>()
                graphNode.maxReaskBudget?.let { loadControlConfig["max_reask_budget"] = it }
                graphNode.requestsPerReask?.let { loadControlConfig["requests_per_reask"] = it }
                put("load_control_config", loadControlConfig)
            }
            graphNode.responsibles?.let { responsibles ->
                put(
                    "responsibles",
                    linkedMapOf(
                        "logins" to responsibles.map { responsible -> quote(responsible) },
                    ),
                )
            }
        }

        val nodeSpec = linkedMapOf<String, Any?>().apply {
            put("backend", "'${graphNode.backendName}'")
            put("params", nodeParams)
            graphNode.neverDiscard?.let { neverDiscard ->
                if (neverDiscard) {
                    put("never_discard", "true")
                }
            }
            graphNode.forceRequestOnEmptyInput?.let { forceRequest ->
                put("force_request_on_empty_input", forceRequest)
            }
        }

        return linkedMapOf(
            "apiVersion" to "v1",
            "kind" to "GraphNode",
            "metadata" to linkedMapOf(
                "node_name" to quote(graphNode.nodeName),
            ),
            "spec" to nodeSpec,
        )
    }

    private fun buildGraphShardedNodeDocument(
        shardedNode: ServicehostGraphShardedNodeConfig,
        backendTestsPayload: ServicehostBackendTestsPayload,
    ): MutableMap<String, Any?> {
        shardedNode.teamcityProject?.let { teamcityProject ->
            backendTestsPayload.components[shardedNode.backendName] = ServicehostBackendTestComponent(
                teamcityJobName = "${teamcityProject}_GitlabCITestApphostGraphs",
            )
        }

        val params = linkedMapOf<String, Any?>(
            "codecs" to shardedNode.codecs,
            "handler" to quote(shardedNode.handler),
        ).apply {
            shardedNode.retryOn?.let { retryOn ->
                put("retry_on", quote(retryOn))
            }
            shardedNode.softTimeout?.let { softTimeout ->
                put("soft_timeout", quote(softTimeout))
            }
            put("timeout", quote(shardedNode.hardTimeout))
        }

        return linkedMapOf(
            "apiVersion" to "v1",
            "kind" to "GraphNodeSharded",
            "metadata" to linkedMapOf(
                "node_name" to quote(shardedNode.nodeName),
            ),
            "spec" to linkedMapOf(
                "shard_count" to shardedNode.shardCount.toString(),
                "backend" to "'${shardedNode.backendName}_{{SHARD}}'",
                "params" to params,
            ),
        )
    }

    private fun quote(rawValue: String): String {
        return "\"$rawValue\""
    }

    private companion object {
        private const val SELF_BACKEND_NAME = "SELF"
    }
}
