package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl

import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.PostProcessedConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope

data class SchedulerI2IServiceConfig (
    override val type: ServiceType = ServiceType.SCHEDULER_I2I,
    override val serviceDisabled: Boolean = false,

    var factorProxyScope: ServiceScope = ServiceScope.PROJECT_SCOPED
) : RecommenderServiceConfig(type, serviceDisabled), PostProcessedConfig {

    override fun postProcess(serviceRuntimes: List<ServiceRuntime>) {
        val factorProxyRuntime = serviceRuntimes.firstOrNull { it.type == ServiceType.FACTOR_PROXY }
            ?: throw IllegalStateException("${ServiceType.FACTOR_PROXY} must be in context")
        this.factorProxyScope = (factorProxyRuntime.config as FactorProxyServiceConfig).serviceScope
    }

}
