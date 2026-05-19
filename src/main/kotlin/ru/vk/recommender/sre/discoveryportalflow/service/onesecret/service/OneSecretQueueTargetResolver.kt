package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.OneSecretQueueTarget
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.OneSecretQueueTargetPlan
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceEnvironmentQueueTargets
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretTargetPlan
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.normalizeQueueTargets
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.resolveQueueTargets
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import kotlin.collections.plusAssign

class OneSecretQueueTargetResolver(
    private val serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
) {

    fun resolveQueueTargetPlan(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntimes: List<ServiceRuntime>,
        dcSettings: RecommenderDcSettings,
    ): OneSecretQueueTargetPlan {
        val projectTargets = mutableListOf<OneSecretQueueTarget>()
        val verticalTargets = mutableListOf<OneSecretQueueTarget>()
        val serviceTargets = mutableListOf<ServiceOneSecretTargetPlan>()

        serviceRuntimes.forEach { serviceRuntime ->
            val environmentQueueTargets = resolveEnvironmentQueueTargets(
                recommenderRuntime = recommenderRuntime,
                serviceRuntime = serviceRuntime,
                dcSettings = dcSettings,
            )
            val queueTargets = environmentQueueTargets.resolveQueueTargets()

            serviceTargets.plusAssign(
                ServiceOneSecretTargetPlan(
                    serviceRuntime = serviceRuntime,
                    environmentQueueTargets = environmentQueueTargets,
                )
            )

            when (serviceRuntime.scope) {
                ServiceScope.PROJECT_SCOPED -> projectTargets += queueTargets
                ServiceScope.VERTICAL_SCOPED, ServiceScope.I2I_VERTICAL_SCOPED -> verticalTargets += queueTargets
            }
        }

        return OneSecretQueueTargetPlan(
            verticalTargets = verticalTargets.normalizeQueueTargets(),
            projectTargets = projectTargets.normalizeQueueTargets(),
            serviceTargets = serviceTargets.sortedBy { it.serviceRuntime.cloudServiceName },
        )
    }

    private fun resolveEnvironmentQueueTargets(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        dcSettings: RecommenderDcSettings,
    ): List<ServiceEnvironmentQueueTargets> {
        val service = serviceRuntimeDefinitionResolver.service(serviceRuntime)
        if (!shouldGenerateOnecloudManifests(service, serviceRuntime)) {
            return emptyList()
        }

        val manifestSpec = buildOnecloudManifest(
            service = service,
            recommenderRuntime = recommenderRuntime,
            dcSettings = dcSettings,
            serviceRuntime = serviceRuntime,
        )

        return serviceRuntime.supportedEnvironments().mapNotNull { environment ->
            val enabled = when (environment) {
                ServiceEnvironment.PRODUCTION ->
                    dcSettings.productionDcs.isNotEmpty()

                ServiceEnvironment.CANARY ->
                    dcSettings.canaryDcs.isNotEmpty() &&
                        manifestSpec.maxCanaryCloudPods > 0

                ServiceEnvironment.TESTING ->
                    dcSettings.testingDcs.isNotEmpty() &&
                        manifestSpec.maxTestingCloudPods > 0
            }

            if (!enabled) {
                return@mapNotNull null
            }

            ServiceEnvironmentQueueTargets(
                environment = environment,
                queueTargets = listOf(
                    OneSecretQueueTarget(
                        cloudNamespace = CLOUD_NAMESPACE,
                        cloudQueueId = serviceRuntime.cloudQueueId(environment),
                    ),
                ),
            )
        }
    }

    private fun <TConfig : RecommenderServiceConfig> shouldGenerateOnecloudManifests(
        service: RecomService<TConfig>,
        serviceRuntime: ServiceRuntime,
    ): Boolean {
        val serviceConfig = serviceRuntime.config.requireConfig(service.configClass)
        return service.shouldGenerateOnecloudManifests(
            serviceRuntime = serviceRuntime,
            serviceConfig = serviceConfig,
        )
    }

    private fun <TConfig : RecommenderServiceConfig> buildOnecloudManifest(
        service: RecomService<TConfig>,
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
    ) = service.buildOnecloudManifest(
        recommenderRuntime = recommenderRuntime,
        createMinOneCloudConfiguration = recommenderRuntime.createMinOneCloudConfiguration,
        dcSettings = dcSettings,
        serviceRuntime = serviceRuntime,
        serviceConfig = serviceRuntime.config.requireConfig(service.configClass),
    )

    private companion object {
        private const val CLOUD_NAMESPACE = "public"
    }
}
