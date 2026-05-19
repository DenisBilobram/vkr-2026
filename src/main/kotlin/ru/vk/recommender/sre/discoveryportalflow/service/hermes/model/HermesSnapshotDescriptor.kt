package ru.vk.recommender.sre.discoveryportalflow.service.hermes.model

data class HermesSnapshotDescriptor(
    val typeId: String,
    val timestampMillis: Long,
) {
    fun toShortName(): String {
        return "$typeId-$timestampMillis"
    }
}
