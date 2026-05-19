package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.OneSecretQueueTarget
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceEnvironmentQueueTargets
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretTargetPlan
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.resolveQueueTargets
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope

class OneSecretProvisioningService(
    private val serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
    private val oneSecretService: OneSecretService,
) {

    fun configureServiceSecrets(
        taskContext: OneSecretTaskContext,
        serviceTarget: ServiceOneSecretTargetPlan,
    ): ServiceOneSecretOutcome {
        val serviceRuntime = serviceTarget.serviceRuntime
        val service = serviceRuntimeDefinitionResolver.service(serviceRuntime)

        return configureServiceSecrets(
            service = service,
            taskContext = taskContext,
            serviceRuntime = serviceRuntime,
            environmentQueueTargets = serviceTarget.environmentQueueTargets,
        )
    }

    private fun <TConfig : RecommenderServiceConfig> configureServiceSecrets(
        service: RecomService<TConfig>,
        taskContext: OneSecretTaskContext,
        serviceRuntime: ServiceRuntime,
        environmentQueueTargets: List<ServiceEnvironmentQueueTargets>,
    ): ServiceOneSecretOutcome {
        if (!service.hasSecret) {
            return ServiceOneSecretOutcome()
        }

        val serviceConfig = serviceRuntime.config.requireConfig(service.configClass)
        val secretData = LinkedHashMap(
            service.buildSecretPairs(
                taskContext = taskContext,
                serviceRuntime = serviceRuntime,
                serviceConfig = serviceConfig,
            ),
        )
        require(secretData.isNotEmpty() || service.allowEmptySecretData) {
            "Service ${serviceRuntime.cloudServiceName} hasSecret=true but buildSecretPairs returned empty map"
        }

        val ownerAbcGroupIds = resolveOwnerAbcGroupIds(taskContext, serviceRuntime)
        val tags = buildServiceSecretTags(service.serviceName, taskContext)

        val sharedSecretId = oneSecretService.configureServiceSecret(
            alias = oneSecretService.buildDiscoverySecretAlias(
                serviceRuntime.cloudServiceName,
                taskContext.recommenderName,
                taskContext.projectName,
            ),
            description = buildServiceSecretDescription(
                cloudServiceName = serviceRuntime.cloudServiceName,
                environment = ServiceEnvironment.PRODUCTION,
            ),
            data = secretData,
            queueTargets = resolveQueueTargets(
                environmentQueueTargets = environmentQueueTargets,
                environments = listOf(ServiceEnvironment.PRODUCTION, ServiceEnvironment.CANARY),
            ),
            ownerAbcGroupIds = ownerAbcGroupIds,
            tags = tags,
        )

        val testingSecretId = if (serviceRuntime.hasTesting) {
            oneSecretService.configureServiceSecret(
                alias = oneSecretService.buildDiscoverySecretAlias(
                    serviceRuntime.environment(ServiceEnvironment.TESTING).cloudServiceName,
                    taskContext.recommenderName,
                    taskContext.projectName,
                ),
                description = buildServiceSecretDescription(
                    cloudServiceName = serviceRuntime.cloudServiceName,
                    environment = ServiceEnvironment.TESTING,
                ),
                data = secretData,
                queueTargets = resolveQueueTargets(
                    environmentQueueTargets = environmentQueueTargets,
                    environments = listOf(ServiceEnvironment.TESTING),
                ),
                ownerAbcGroupIds = ownerAbcGroupIds,
                tags = tags,
            )
        } else {
            null
        }

        return ServiceOneSecretOutcome(
            sharedSecretId = sharedSecretId,
            testingSecretId = testingSecretId,
        )
    }

    private fun buildServiceSecretDescription(
        cloudServiceName: String,
        environment: ServiceEnvironment,
    ): String {
        return "${environment.id.replaceFirstChar { it.uppercase() }} service secret for $cloudServiceName"
    }

    private fun buildServiceSecretTags(
        serviceName: String,
        taskContext: OneSecretTaskContext,
    ): List<String> {
        return buildList {
            add(serviceName)
            add(taskContext.recommenderName)
            taskContext.projectName?.takeIf { it.isNotBlank() }?.let(::add)
        }
    }

    private fun resolveOwnerAbcGroupIds(
        taskContext: OneSecretTaskContext,
        serviceRuntime: ServiceRuntime,
    ): List<Int> {
        return when (serviceRuntime.scope) {
            ServiceScope.PROJECT_SCOPED -> listOf(requireNotNull(taskContext.projectProductId) {
                "recommender.projectProductId is required to create service secret for ${serviceRuntime.cloudServiceName}"
            })

            ServiceScope.VERTICAL_SCOPED, ServiceScope.I2I_VERTICAL_SCOPED -> buildList {
                add(requireNotNull(taskContext.productId) {
                    "recommender.productId is required to create service secret for ${serviceRuntime.cloudServiceName}"
                })
                taskContext.projectProductId?.let(::add)
            }
        }
    }

    private fun resolveQueueTargets(
        environmentQueueTargets: List<ServiceEnvironmentQueueTargets>,
        environments: List<ServiceEnvironment>,
    ): List<OneSecretQueueTarget> {
        return environmentQueueTargets.resolveQueueTargets(environments)
    }
}
