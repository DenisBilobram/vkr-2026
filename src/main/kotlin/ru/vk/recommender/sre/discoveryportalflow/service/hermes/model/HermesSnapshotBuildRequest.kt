package ru.vk.recommender.sre.discoveryportalflow.service.hermes.model

data class HermesSnapshotBuildRequest(
    val taskId: String,
    val description: String,
    val body: Map<String, Any>,
)
