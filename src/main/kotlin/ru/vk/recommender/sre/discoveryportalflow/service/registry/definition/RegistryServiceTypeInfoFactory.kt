package ru.vk.recommender.sre.discoveryportalflow.service.registry.definition

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextFieldInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextFieldValidationInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineServiceTypeInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineSimpleContextFieldInfo
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.field.PipelineContextFieldFactory
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomServiceRegistry
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.RecommenderServiceConfigDefaults

@Component
class RegistryServiceTypeInfoFactory(
    private val recomServiceRegistry: RecomServiceRegistry,
    private val fieldFactory: PipelineContextFieldFactory,
) {

    fun serviceTypeInfos(
        requiredServiceTypes: List<ServiceType>,
        optionalServiceTypes: List<ServiceType>,
    ): List<PipelineServiceTypeInfo> {
        val serviceTypes = requiredServiceTypes + optionalServiceTypes
        require(serviceTypes.distinct().size == serviceTypes.size) {
            "Service list contains duplicated service types: ${serviceTypes.joinToString { it.name }}"
        }

        val required = requiredServiceTypes.toSet()
        val servicesByType = recomServiceRegistry.allServices().associateBy { service -> service.type }
        return serviceTypes.map { serviceType ->
            serviceTypeInfo(
                serviceType = serviceType,
                required = serviceType in required,
                servicesByType = servicesByType,
            )
        }
    }

    private fun serviceTypeInfo(
        serviceType: ServiceType,
        required: Boolean,
        servicesByType: Map<ServiceType, RecomService<*>>,
    ): PipelineServiceTypeInfo {
        val service = servicesByType[serviceType]
            ?: error("No service definition configured for type ${serviceType.name}")

        val fields = serviceFields(serviceType)
        return fieldFactory.serviceTypeInfo(
            type = serviceType.name,
            label = service.serviceName,
            required = required,
            defaultValue = buildMap {
                put("type", serviceType.name)
                fields.filterIsInstance<PipelineSimpleContextFieldInfo>()
                    .filter { it.defaultValue != null }
                    .forEach { field -> put(field.path, field.defaultValue) }
            },
            fields = fields,
        )
    }

    private fun serviceFields(serviceType: ServiceType): List<PipelineContextFieldInfo> {
        return when (serviceType) {
            ServiceType.BASE -> listOf(hasSnapshotsField(defaultValue = true), shardsCountField())
            ServiceType.BASE_I2I -> listOf(
                hasSnapshotsField(defaultValue = true),
                shardsCountField(defaultValue = RecommenderServiceConfigDefaults.BASE_I2I_SHARDS_COUNT),
            )

            ServiceType.META_I2I -> listOf(hasSnapshotsField(defaultValue = true))
            ServiceType.META,
            ServiceType.GATEWAY,
            ServiceType.PLATFORM_GATEWAY,
            ServiceType.SELECTORS -> listOf(hasSnapshotsField(defaultValue = false))

            ServiceType.MEDIATOR -> listOf(
                fieldFactory.checkboxField(
                    name = "withCache",
                    path = "withCache",
                    label = "With cache",
                    defaultValue = false,
                ),
            )

            ServiceType.FACTOR_PROXY,
            ServiceType.YT_PROXY,
            ServiceType.SNAPSHOTS_BUILDER,
            ServiceType.GRPC_PROXY,
            ServiceType.WORKER,
            ServiceType.SCHEDULER_I2I -> emptyList()
        }
    }

    private fun hasSnapshotsField(defaultValue: Boolean): PipelineContextFieldInfo {
        return fieldFactory.checkboxField(
            name = "hasSnapshots",
            path = "hasSnapshots",
            label = "With snapshots",
            defaultValue = defaultValue,
        )
    }

    private fun shardsCountField(
        defaultValue: Int = RecommenderServiceConfigDefaults.BASE_SHARDS_COUNT,
    ): PipelineContextFieldInfo {
        return fieldFactory.intField(
            name = "shardsCount",
            path = "shardsCount",
            label = "Shards count",
            required = true,
            defaultValue = defaultValue,
            validation = PipelineContextFieldValidationInfo(min = 1, max = 32),
        )
    }
}
