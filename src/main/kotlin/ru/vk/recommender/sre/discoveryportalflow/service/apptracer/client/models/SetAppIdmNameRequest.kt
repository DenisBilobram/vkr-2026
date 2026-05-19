package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models

data class SetAppIdmNameRequest(
    val appId: Long,
    val idmName: String,
)

typealias SetAppIdmNameResponse = SuccessResponse
