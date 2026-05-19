package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models

data class SetAppIdmRoleOwnersRequest(
    val appId: Long,
    val idmRoleOwners: List<String>,
)

typealias SetAppIdmRoleOwnersResponse = SuccessResponse
