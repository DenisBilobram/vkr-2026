package ru.vk.recommender.sre.discoveryportalflow.service.recom.model

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope

interface ScopedServiceConfig {
    val serviceScope: ServiceScope
}

interface TenantServiceConfig {
    val tenant: String?
}

interface ShardedServiceConfig {
    val shardsCount: Int
}

interface PostProcessedConfig {
    fun postProcess(serviceRuntimes: List<ServiceRuntime>)
}
