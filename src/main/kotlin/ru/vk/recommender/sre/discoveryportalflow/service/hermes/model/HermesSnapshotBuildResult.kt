package ru.vk.recommender.sre.discoveryportalflow.service.hermes.model

data class HermesSnapshotBuildResult(
    val builderBaseUrl: String,
    val triggeredRequestsCount: Int,
)
