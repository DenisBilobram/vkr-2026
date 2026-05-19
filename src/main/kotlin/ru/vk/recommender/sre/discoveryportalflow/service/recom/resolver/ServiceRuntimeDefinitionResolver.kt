package ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomServiceRegistry
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

@Component
class ServiceRuntimeDefinitionResolver(
    private val recomServiceRegistry: RecomServiceRegistry,
) {

    fun service(serviceRuntime: ServiceRuntime): RecomService<*> {
        return recomServiceRegistry.serviceByType(serviceRuntime.type)
    }
}
