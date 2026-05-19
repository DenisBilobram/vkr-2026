package ru.vk.recommender.sre.discoveryportalflow.api.runner.payload

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.api.runner.model.StageTasksPayload

object StageTasksContextMapper {

    private val objectMapper = jacksonObjectMapper()

    fun toJsonNode(stageTasksPayload: StageTasksPayload): JsonNode {
        return objectMapper.valueToTree(stageTasksPayload)
    }
}
