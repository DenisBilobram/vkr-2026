package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment

data class ServiceEnvironmentQueueTargets(
    val environment: ServiceEnvironment,
    val queueTargets: List<OneSecretQueueTarget>,
)

fun Iterable<ServiceEnvironmentQueueTargets>.resolveQueueTargets(
    environments: Iterable<ServiceEnvironment>? = null,
): List<OneSecretQueueTarget> {
    val allowedEnvironments = environments?.toSet()
    return asSequence()
        .filter { environmentTargets ->
            allowedEnvironments == null || environmentTargets.environment in allowedEnvironments
        }
        .flatMap { environmentTargets -> environmentTargets.queueTargets.asSequence() }
        .toList()
        .normalizeQueueTargets()
}
