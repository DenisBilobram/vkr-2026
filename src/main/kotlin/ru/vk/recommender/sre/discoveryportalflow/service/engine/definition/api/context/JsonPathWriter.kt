package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Component

@Component
class JsonPathWriter(
    private val objectMapper: ObjectMapper,
) {

    fun set(
        target: ObjectNode,
        path: String,
        value: JsonNode,
    ) {
        val segments = path.split('.')
        require(segments.none(String::isBlank)) {
            "Invalid context path '$path'"
        }

        var current = target
        segments.dropLast(1).forEach { segment ->
            val child = current.get(segment)
            current = if (child is ObjectNode) {
                child
            } else {
                objectMapper.createObjectNode().also { newChild ->
                    current.set<ObjectNode>(segment, newChild)
                }
            }
        }
        current.set<JsonNode>(segments.last(), value.deepCopy())
    }
}
