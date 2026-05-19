package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models

data class GetAppTracerRequest(val appId: Long)

data class GetAppTracerResponse(val items: List<TracerApplication> = emptyList(), val count: Int? = null)

data class ListAppsTracerRequest(
    val count: Int? = null,
    val marker: String? = null,
    val orgId: Long,
)

data class ListAppsTracerResponse(
    val items: List<TracerApplication> = emptyList(),
    val count: Int,
    val marker: String?,
    val hasMore: Boolean,
)

data class TracerApplication(
    val id: Long,
    val orgId: Long,
    val platform: TracerPlatform,
    val type: TracerType,
    val name: String,
    val mappingUploadToken: String,
    val sampleUploadToken: String,
    val userCount: Long,
    val alertsCreated: Long,
    val idmName: String? = null,
    val idmRoleOwners: List<String>? = null,
    val features: String? = null,
    val featuresOptions: Map<String, Any>? = null,
    val quota: Quota? = null,
    val storageUsage: Long? = null,
    val mappingCount: Long? = null,
    val sampleCount: Long? = null,
    val dailyCounters: DailyCounters? = null,
    val status: String? = null,
)
