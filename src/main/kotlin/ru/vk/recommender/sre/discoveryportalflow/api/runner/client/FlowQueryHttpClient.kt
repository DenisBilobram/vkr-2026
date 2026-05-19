package ru.vk.recommender.sre.discoveryportalflow.api.runner.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunsPageResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineStageRunsDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageRunDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.TaskLogInfo
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

class FlowQueryHttpClient(
    private val flowBaseUrl: String,
    private val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules(),
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
) {

    fun getPipelineRuns(page: Int): PipelineRunsPageResponse {
        return get("/flow/pipelines?page=$page", object : TypeReference<PipelineRunsPageResponse>() {})
    }

    fun getPipelineRunDetails(pipelineRunId: UUID): PipelineRunDetailsResponse {
        return get("/flow/pipeline/$pipelineRunId", object : TypeReference<PipelineRunDetailsResponse>() {})
    }

    fun getPipelineStageRunsDetails(pipelineRunId: UUID): PipelineStageRunsDetailsResponse {
        return get("/flow/pipeline/$pipelineRunId/stages/details", object : TypeReference<PipelineStageRunsDetailsResponse>() {})
    }

    fun getStageRunDetails(stageRunId: UUID): StageRunDetailsResponse {
        return get("/flow/stage/$stageRunId", object : TypeReference<StageRunDetailsResponse>() {})
    }

    fun getTaskLogs(taskRunId: UUID): List<TaskLogInfo> {
        return get("/flow/task/$taskRunId/logs", object : TypeReference<List<TaskLogInfo>>() {})
    }

    private fun <T> get(
        endpoint: String,
        responseType: TypeReference<T>,
    ): T {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$flowBaseUrl$endpoint"))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        require(response.statusCode() in 200..299) {
            "GET '$endpoint' failed with status ${response.statusCode()}: ${response.body()}"
        }

        return objectMapper.readValue(response.body(), responseType)
    }
}
