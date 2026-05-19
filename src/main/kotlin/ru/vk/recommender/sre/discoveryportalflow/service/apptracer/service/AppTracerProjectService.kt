package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.service

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.AppTracerClient
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.TracerCallResult
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.AddAppTracerRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.GetAppTracerRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.ListAppsTracerRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.SetAppIdmNameRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.SetAppIdmRoleOwnersRequest
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.TracerApplication
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.TracerPlatform
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models.TracerType
import ru.vk.recommender.sre.discoveryportalflow.service.engine.logging.TasksRuntimeLogger

sealed interface ProjectSetupResult {
    data class Success(val token: String) : ProjectSetupResult
    data class Error(val message: String, val cause: Throwable? = null) : ProjectSetupResult
}

class AppTracerProjectService(
    private val appTracerClient: AppTracerClient,
    private val objectMapper: ObjectMapper,
    private val runtimeLogger: TasksRuntimeLogger,
) {

    suspend fun createOrGetApp(
        projectName: String,
        orgId: Long,
        idmRoleOwners: List<String>,
    ): ProjectSetupResult {
        runtimeLogger.info("Creating AppTracer project '$projectName' in orgId=$orgId")

        val appId = when (
            val addAppResult = appTracerClient.addApp(
                AddAppTracerRequest(
                    orgId = orgId,
                    platform = TracerPlatform.BACKEND,
                    type = TracerType.APPLICATION,
                    name = projectName,
                ),
            )
        ) {
            is TracerCallResult.Success -> addAppResult.value.id
            is TracerCallResult.Failure -> {
                if (addAppResult.status == 400 && isDuplicateAppNameError(addAppResult.responseBody)) {
                    runtimeLogger.warn("AppTracer project already exists: $projectName")
                    findAppIdByName(orgId, projectName)?.also { appId ->
                        runtimeLogger.info("Found AppTracer appId=$appId for project '$projectName'")
                    } ?: return ProjectSetupResult.Error(
                        "AppTracer project '$projectName' already exists, but appId was not found in orgId=$orgId",
                    )
                } else {
                    logFailure("Failed to create AppTracer project '$projectName'", addAppResult)
                    return ProjectSetupResult.Error(
                        "Failed to create AppTracer project '$projectName': status=${addAppResult.status}",
                    )
                }
            }
        }

        runtimeLogger.info("Continuing AppTracer setup for appId=$appId")

        if (!setIdmName(appId, buildIdmName(projectName))) {
            return ProjectSetupResult.Error("Failed to set IDM name for project $appId")
        }

        if (!setIdmRoleOwners(appId, idmRoleOwners)) {
            return ProjectSetupResult.Error("Failed to set IDM owners for project $appId")
        }

        val project = getProjectDetails(appId)
            ?: return ProjectSetupResult.Error("Failed to get project details for $appId")

        runtimeLogger.info("Retrieved AppTracer project details for appId=${project.id}")
        return ProjectSetupResult.Success(project.sampleUploadToken)
    }

    private suspend fun setIdmName(appId: Long, idmName: String): Boolean {
        return when (
            val result = appTracerClient.setAppIdmName(
                SetAppIdmNameRequest(appId = appId, idmName = idmName),
            )
        ) {
            is TracerCallResult.Success -> {
                if (!result.value.success) {
                    runtimeLogger.error("Failed to set IDM name for project $appId: success=false")
                    false
                } else {
                    runtimeLogger.info("Set IDM name '$idmName' for project $appId")
                    true
                }
            }

            is TracerCallResult.Failure -> {
                if (result.status == 400 && isDuplicateIdmNameError(result.responseBody)) {
                    runtimeLogger.warn(
                        "IDM name '$idmName' is already in use for project $appId; continuing. " +
                            "status=${result.status}, body=${result.responseBody}",
                    )
                    true
                } else {
                    logFailure("Failed to set IDM name for project $appId", result)
                    false
                }
            }
        }
    }

    private suspend fun setIdmRoleOwners(appId: Long, idmRoleOwners: List<String>): Boolean {
        return when (
            val result = appTracerClient.setAppIdmRoleOwners(
                SetAppIdmRoleOwnersRequest(appId = appId, idmRoleOwners = idmRoleOwners),
            )
        ) {
            is TracerCallResult.Success -> {
                if (!result.value.success) {
                    runtimeLogger.error("Failed to set IDM owners for project $appId: success=false")
                    false
                } else {
                    runtimeLogger.info("Set IDM owners for project $appId")
                    true
                }
            }

            is TracerCallResult.Failure -> {
                logFailure("Failed to set IDM owners for project $appId", result)
                false
            }
        }
    }

    private suspend fun getProjectDetails(appId: Long): TracerApplication? {
        return when (val result = appTracerClient.getApp(GetAppTracerRequest(appId = appId))) {
            is TracerCallResult.Success -> {
                val project = result.value.items.firstOrNull()
                if (project == null) {
                    runtimeLogger.error("Failed to get project details for $appId: empty items list")
                }
                project
            }

            is TracerCallResult.Failure -> {
                logFailure("Failed to get project details for $appId", result)
                null
            }
        }
    }

    private suspend fun findAppIdByName(orgId: Long, projectName: String): Long? {
        var marker: String? = null

        while (true) {
            when (
                val result = appTracerClient.listApps(
                    ListAppsTracerRequest(count = LIST_APPS_PAGE_SIZE, orgId = orgId, marker = marker),
                )
            ) {
                is TracerCallResult.Success -> {
                    val appId = result.value.items.firstOrNull { it.name == projectName }?.id
                    if (appId != null) {
                        return appId
                    }

                    if (!result.value.hasMore) {
                        runtimeLogger.info("AppTracer project '$projectName' not found in orgId=$orgId")
                        return null
                    }
                    marker = result.value.marker
                }

                is TracerCallResult.Failure -> {
                    logFailure("Failed to list AppTracer projects for orgId=$orgId", result)
                    return null
                }
            }
        }
    }

    private fun isDuplicateAppNameError(errorBody: String?): Boolean {
        return isDuplicateValidationError(errorBody, "Application name should be unique")
    }

    private fun isDuplicateIdmNameError(errorBody: String?): Boolean {
        return isDuplicateValidationError(errorBody, "Application idm name should be unique")
    }

    private fun isDuplicateValidationError(errorBody: String?, expectedMessage: String): Boolean {
        if (errorBody == null) return false
        return try {
            val errorJson = objectMapper.readTree(errorBody)
            val errorCode = errorJson.path("errorCode").asText()
            val message = errorJson.path("message").asText()
            errorCode == "INVALID_PARAMETERS" && message.contains(expectedMessage, ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun logFailure(
        message: String,
        result: TracerCallResult.Failure,
    ) {
        runtimeLogger.error("$message: status=${result.status}, body=${result.responseBody}")
    }

    private fun buildIdmName(projectName: String): String {
        return projectName.lowercase().replace(" ", "").replace("-", "")
    }

    private companion object {
        private const val LIST_APPS_PAGE_SIZE = 300
    }
}
