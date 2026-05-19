package ru.vk.recommender.sre.discoveryportalflow.service.vkteams.client.request

// NOTE: members must use the format expected by the configured messenger implementation.
data class CreateChatRequest(
    val name: String? = null,
    val about: String? = null,
    val rules: String? = null,
    val members: List<Map<String, String>>? = null,
    val isPublic: Boolean? = null,
    val defaultRole: String? = null,
    val joinModeration: String? = null
)
