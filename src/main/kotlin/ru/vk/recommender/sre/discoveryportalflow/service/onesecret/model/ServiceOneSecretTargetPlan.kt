package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

data class ServiceOneSecretTargetPlan(
    val serviceRuntime: ServiceRuntime,
    val environmentQueueTargets: List<ServiceEnvironmentQueueTargets>,
)
