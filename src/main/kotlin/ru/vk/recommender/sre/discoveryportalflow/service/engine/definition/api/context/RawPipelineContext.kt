package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class RawPipelineContext(
    private val rawContext: ObjectNode,
) {

    fun entries(): Sequence<RawPipelineContextEntry> {
        return rawContext.fields().asSequence()
            .map { entry -> RawPipelineContextEntry(path = entry.key, value = entry.value) }
    }

    fun entriesWithPrefix(prefix: String): Sequence<RawPipelineContextEntry> {
        return entries().filter { entry -> entry.path.startsWith("$prefix.") }
    }

    fun value(path: String): JsonNode? {
        return rawContext.get(path)
    }

    fun booleanValue(path: String): Boolean? {
        val value = value(path) ?: return null
        return value.takeIf(JsonNode::isBoolean)?.asBoolean()
    }
}

data class RawPipelineContextEntry(
    val path: String,
    val value: JsonNode,
)
