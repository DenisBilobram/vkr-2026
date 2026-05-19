package ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.RuntimeTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomServiceRegistry
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.PostProcessedConfig

@Component
class RecomRuntimeContextFactory(
    private val recomServiceRegistry: RecomServiceRegistry,
) {

    fun build(stageContext: BootstrapRecomContext): RuntimeTaskContext {
        val recommenderRuntime = RecommenderRuntime(stageContext)
        val serviceRuntimes = stageContext.services.map { serviceConfig ->
            val service = recomServiceRegistry.serviceByType(serviceConfig.type)
            ServiceRuntime.create(
                recommenderRuntime = recommenderRuntime,
                service = service,
                serviceConfig = serviceConfig,
            )
        }

        serviceRuntimes
            .filter { serviceRuntime -> serviceRuntime.config is PostProcessedConfig }
            .forEach { serviceRuntime -> (serviceRuntime.config as PostProcessedConfig).postProcess(serviceRuntimes) }

        return RuntimeTaskContext(
            recommender = recommenderRuntime,
            services = serviceRuntimes,
            dcSettings = stageContext.dcSettings,
            servicehostClusterName = stageContext.servicehostClusterName,
            branch = stageContext.branch,
            teamsChatId = stageContext.teamsChatId,
        )
    }
}
