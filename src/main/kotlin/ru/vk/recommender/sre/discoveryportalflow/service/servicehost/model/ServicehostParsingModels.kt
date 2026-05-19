package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// Data classes for parsing YAML content
@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphItem(
    val kind: String? = null,
    val metadata: GraphMetadata? = null,
    val spec: GraphSpec? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphMetadata(
    @JsonProperty("node_name") val nodeName: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphSpec(
    val backend: String? = null,
    val params: Map<String, Any>? = null
)

data class Graph(
    val name: String,
    val backend: String,
    val handler: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BackendMetadata(
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("cloud_service_name") val cloudServiceName: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BackendItem(
    val kind: String? = null,
    val metadata: BackendMetadata? = null,
)

data class Backend(val name: String?, val cloudServiceName: String?)

data class Node(
    val name: String,
    val backend: String,
    val handler: String
)
