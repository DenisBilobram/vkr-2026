package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.AddAppTracerRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.AddAppTracerResponse
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.GetAppTracerRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.GetAppTracerResponse
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.ListAppsTracerRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.ListAppsTracerResponse
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.SetAppIdmNameRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.SetAppIdmNameResponse
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.SetAppIdmRoleOwnersRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.SetAppIdmRoleOwnersResponse
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.SetOrgIdmNameRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.SetOrgIdmNameResponse
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.config.TracerClientProperties

sealed interface TracerCallResult<out T> {
    data class Success<T>(
        val value: T,
        val status: Int,
    ) : TracerCallResult<T>

    data class Failure(
        val operationName: String,
        val endpoint: String,
        val status: Int?,
        val requestBody: String,
        val responseBody: String?,
        val cause: Throwable? = null,
        val isRetry: Boolean = false,
    ) : TracerCallResult<Nothing>
}

class AppTracerClient(
    private val objectMapper: ObjectMapper,
    private val properties: TracerClientProperties,
) {

    suspend fun listApps(request: ListAppsTracerRequest): TracerCallResult<ListAppsTracerResponse> =
        ndaFailure("listApps", request)

    suspend fun getApp(request: GetAppTracerRequest): TracerCallResult<GetAppTracerResponse> =
        ndaFailure("getApp", request)

    suspend fun addApp(request: AddAppTracerRequest): TracerCallResult<AddAppTracerResponse> =
        ndaFailure("addApp", request)

    suspend fun setOrgIdmName(request: SetOrgIdmNameRequest): TracerCallResult<SetOrgIdmNameResponse> =
        ndaFailure("setOrgIdmName", request)

    suspend fun setAppIdmName(request: SetAppIdmNameRequest): TracerCallResult<SetAppIdmNameResponse> =
        ndaFailure("setAppIdmName", request)

    suspend fun setAppIdmRoleOwners(
        request: SetAppIdmRoleOwnersRequest,
    ): TracerCallResult<SetAppIdmRoleOwnersResponse> =
        ndaFailure("setAppIdmRoleOwners", request)

    private fun <T> ndaFailure(operationName: String, request: Any): TracerCallResult<T> {
        // NDA code removed: production implementation refreshes tokens and calls internal AppTracer APIs.
        return TracerCallResult.Failure(
            operationName = operationName,
            endpoint = properties.url,
            status = null,
            requestBody = runCatching { objectMapper.writeValueAsString(request) }.getOrDefault(request.toString()),
            responseBody = "NDA code removed",
        )
    }
}
