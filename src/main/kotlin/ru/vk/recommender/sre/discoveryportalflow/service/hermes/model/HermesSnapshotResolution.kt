package ru.vk.recommender.sre.discoveryportalflow.service.hermes.model

data class HermesSnapshotResolution(
    val resolvedSnapshots: List<HermesSnapshotDescriptor>,
    val missingTypeIds: List<String>,
)
