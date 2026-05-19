package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models

data class SetOrgIdmNameRequest(
    val appId: Long,
    val idmName: String,
)

typealias SetOrgIdmNameResponse = SuccessResponse
