package ru.vk.recommender.sre.discoveryportalflow.service.ocs.client

import com.fasterxml.jackson.databind.ObjectMapper

class OcsClient(
    private val objectMapper: ObjectMapper,
) {

    fun putJson(relativePath: String, payload: Any, expectedStatusCode: Int = HTTP_STATUS_CREATED) {
        ndaStub("putJson", relativePath, payload)
    }

    fun postJson(relativePath: String, payload: Any, expectedStatusCode: Int = HTTP_STATUS_CREATED) {
        ndaStub("postJson", relativePath, payload)
    }

    private fun ndaStub(operation: String, relativePath: String, payload: Any) {
        // NDA code removed: production implementation calls an internal one-click service API.
        objectMapper.createObjectNode()
            .put("operation", operation)
            .put("relativePath", relativePath)
            .put("payload", payload.toString())
    }

    private companion object {
        private const val HTTP_STATUS_CREATED = 201
    }
}
