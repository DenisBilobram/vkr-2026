package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType

@Component
class RecomServiceRegistry(
    services: List<RecomService<*>>,
) {
    private val servicesByType: Map<ServiceType, RecomService<*>> = buildServicesByType(services)

    fun serviceByType(serviceType: ServiceType): RecomService<*> {
        return servicesByType[serviceType]
            ?: error("No service definition configured for type ${serviceType.name}")
    }

    fun allServices(): Collection<RecomService<*>> {
        return servicesByType.values
    }

    private fun buildServicesByType(services: List<RecomService<*>>): Map<ServiceType, RecomService<*>> {
        val definitionsByType = services.groupBy { service -> service.type }
        val duplicateTypes = definitionsByType.filterValues { definitions -> definitions.size > 1 }.keys
        require(duplicateTypes.isEmpty()) {
            "Multiple service definitions are configured for types: ${duplicateTypes.joinToString(", ")}"
        }

        return definitionsByType.mapValues { (_, definitions) -> definitions.single() }
    }
}
