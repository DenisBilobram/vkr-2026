package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment

data class ServiceOneSecretOutcome(
    val sharedSecretId: String? = null,
    val testingSecretId: String? = null,
) {
    fun secretIdFor(environment: ServiceEnvironment): String? {
        return when (environment) {
            ServiceEnvironment.TESTING -> testingSecretId
            ServiceEnvironment.PRODUCTION,
            ServiceEnvironment.CANARY -> sharedSecretId
        }
    }

    val secretCreated: Boolean
        get() = !sharedSecretId.isNullOrBlank() || !testingSecretId.isNullOrBlank()
}
