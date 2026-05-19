package ru.vk.recommender.sre.discoveryportalflow.service.hermes.client

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.model.HermesSnapshotBuildRequest
import java.net.http.HttpClient

class SnapshotsBuilderClient(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
) {

    fun trigger(
        builderBaseUrl: String,
        request: HermesSnapshotBuildRequest,
    ) {
        // NDA code removed: production implementation sends snapshot build requests to an internal API.
        objectMapper.writeValueAsString(request.body)
    }
}
