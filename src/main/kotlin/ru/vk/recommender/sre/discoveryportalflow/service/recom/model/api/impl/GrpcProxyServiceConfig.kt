package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig

data class GrpcProxyServiceConfig(
    override val type: ServiceType = ServiceType.GRPC_PROXY,
    override val serviceDisabled: Boolean = false,
    val existingService: Boolean = false,
    val cloudServiceName: String? = null,
) : RecommenderServiceConfig(type, serviceDisabled) {
    init {
        require(!existingService || !cloudServiceName.isNullOrBlank()) {
            "grpc-proxy existingService=true requires cloudServiceName"
        }
    }
}
