package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Component

sealed interface ContextRunDecision {
    data object Run : ContextRunDecision
    data class Skip(val flagPath: String) : ContextRunDecision
}

@Component
class ContextFlagEvaluator {

    fun resolveRunDecision(
        subjectDescription: String,
        executeIfPath: String?,
        contextJson: JsonNode,
    ): ContextRunDecision {
        executeIfPath ?: return ContextRunDecision.Run
        val executeIfFlag = readExecuteIfFlag(contextJson, executeIfPath)
            ?: error("$subjectDescription requires execute-if flag '$executeIfPath' in context, but it is missing")

        require(executeIfFlag.isBoolean) {
            "$subjectDescription execute-if flag '$executeIfPath' must be boolean, but was ${executeIfFlag.nodeType}"
        }

        return if (executeIfFlag.booleanValue()) {
            ContextRunDecision.Run
        } else {
            ContextRunDecision.Skip(flagPath = executeIfPath)
        }
    }

    private fun readExecuteIfFlag(
        contextJson: JsonNode,
        executeIfPath: String,
    ): JsonNode? {
        return executeIfPath
            .split('.')
            .fold(contextJson) { currentNode, pathSegment ->
                currentNode.get(pathSegment) ?: return null
            }
            .takeUnless { resolvedNode -> resolvedNode.isNull || resolvedNode.isMissingNode }
    }
}
