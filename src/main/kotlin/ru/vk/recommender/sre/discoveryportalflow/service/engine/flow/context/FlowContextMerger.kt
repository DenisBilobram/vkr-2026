package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Component

@Component
class FlowContextMerger {

    fun merge(currentContext: ObjectNode, contextUpdate: JsonNode): ObjectNode {
        require(contextUpdate is ObjectNode) { "Context update must be a JSON object" }
        return mergeObjects(currentContext.deepCopy(), contextUpdate)
    }

    private fun mergeObjects(target: ObjectNode, patch: ObjectNode): ObjectNode {
        val fields = patch.fields()
        while (fields.hasNext()) {
            val entry = fields.next()
            val fieldName = entry.key
            val patchValue = entry.value
            val currentValue = target.get(fieldName)

            if (currentValue is ObjectNode && patchValue is ObjectNode) {
                mergeObjects(currentValue, patchValue)
            } else {
                target.set<JsonNode>(fieldName, patchValue.deepCopy())
            }
        }
        return target
    }
}
