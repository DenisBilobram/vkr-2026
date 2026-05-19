package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.models

enum class TracerPlatform {
    BACKEND,
    FRONTEND,
}

enum class TracerType {
    APPLICATION,
}

data class DailyCounters(
    val name: String? = null,
    val value: Long,
)

data class Quota(
    val userCount: Long,
)
