package ru.vk.recommender.sre.discoveryportalflow.service.pms.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.pms.context.ServicePmsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.enabledServices
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver

class ServicePmsSubmitTask(
    private val serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
) : FlowTask<ServicePmsTaskContext>(ServicePmsTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: ServicePmsTaskContext): TaskRunResult {
        val apptracerConfiguredServices = mutableListOf<String>()
        taskRunContext.services.enabledServices().forEach { serviceRuntime ->
            val submitOutcome = submitPmsConfig(
                service = serviceRuntimeDefinitionResolver.service(serviceRuntime),
                taskContext = taskRunContext,
                serviceRuntime = serviceRuntime,
            )
            if (submitOutcome.apptracerConfigured) {
                apptracerConfiguredServices += serviceRuntime.cloudServiceName
            }
        }

        runtimeLogger.info(
            "Submitted PMS configs for ${taskRunContext.recommenderName}: " +
                "apptracer=${apptracerConfiguredServices.size}",
        )

        if (apptracerConfiguredServices.isNotEmpty()) {
            runtimeLogger.info("Apptracer PMS services: ${apptracerConfiguredServices.joinToString(", ")}")
        }

        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    private fun <TConfig : RecommenderServiceConfig> submitPmsConfig(
        service: RecomService<TConfig>,
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
    ) = service.submitPmsConfig(
        taskContext = taskContext,
        serviceRuntime = serviceRuntime,
        serviceConfig = serviceRuntime.config.requireConfig(service.configClass),
    )
}
