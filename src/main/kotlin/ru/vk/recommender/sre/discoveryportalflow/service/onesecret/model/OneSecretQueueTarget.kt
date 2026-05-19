package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model

data class OneSecretQueueTarget(
    val cloudNamespace: String,
    val cloudQueueId: String,
) {
    fun stableKey(): String {
        return "$cloudNamespace/$cloudQueueId"
    }
}

fun Iterable<OneSecretQueueTarget>.normalizeQueueTargets(): List<OneSecretQueueTarget> {
    return distinctBy(OneSecretQueueTarget::stableKey)
        .sortedBy(OneSecretQueueTarget::stableKey)
}
