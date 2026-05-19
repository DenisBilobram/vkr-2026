package ru.vk.recommender.sre.discoveryportalflow.service.hermes.model

data class HermesSnapshotBuildPlan(
    val builderBaseUrl: String,
    val requests: List<HermesSnapshotBuildRequest>,
)
