package ru.vk.recommender.sre.discoveryportalflow.api.runner.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunActionResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunRequest
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageRunActionResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

class FlowInvocationHttpClient(
    private val flowBaseUrl: String,
    private val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules(),
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
) {

    fun createPipeline(
        pipelineName: String,
        pipelineContext: JsonNode,
    ): PipelineRunActionResponse {
        return post(
            endpoint = "/flow/pipeline/create",
            requestBody = PipelineRunRequest(
                pipelineName = pipelineName,
                pipelineContext = pipelineContext.deepCopy(),
            ),
            responseType = PipelineRunActionResponse::class.java,
        )
    }

    fun startStage(stageRunId: UUID): StageRunActionResponse {
        return post(
            endpoint = "/flow/stage/$stageRunId/start",
            requestBody = null,
            responseType = StageRunActionResponse::class.java,
        )
    }

    private fun <T> post(
        endpoint: String,
        requestBody: Any?,
        responseType: Class<T>,
    ): T {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("$flowBaseUrl$endpoint"))

        val request = if (requestBody == null) {
            requestBuilder.POST(HttpRequest.BodyPublishers.noBody())
        } else {
            requestBuilder
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
        }.build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        require(response.statusCode() in 200..299) {
            "POST '$endpoint' failed with status ${response.statusCode()}: ${response.body()}"
        }

        return objectMapper.readValue(response.body(), responseType)
    }
}
