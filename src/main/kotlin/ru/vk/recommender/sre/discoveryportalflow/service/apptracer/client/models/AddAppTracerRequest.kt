package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models

data class AddAppTracerRequest(
    val orgId: Long,
    val platform: TracerPlatform,
    val type: TracerType,
    val name: String,
)

data class AddAppTracerResponse(
    val id: Long,
)
