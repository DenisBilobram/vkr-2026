package ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ShardedServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.TenantServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.RecommenderNames
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames

@JsonIgnoreProperties(ignoreUnknown = true)
data class ServiceRuntime(
    val type: ServiceType,
    val config: RecommenderServiceConfig,
    val namespace: String,
    val serviceName: String,
    val cloudServiceName: String,
    val onecloudDirectoryName: String = "",
    val teamcityProject: String,
    val sourceDirectory: String,
    val gradleBuildCommand: String,
    val serviceProductionRootQueueName: String,
    val serviceTestingRootQueueName: String,
    val pmsRootQueueName: String = "",
    val scope: ServiceScope,
    val tenant: String?,
    val onecloudSubqueues: List<String>,
    val hasTesting: Boolean,
    val hasCanary: Boolean,
    val hasSnapshots: Boolean,
    val hasVector: Boolean,
    val names: RecommenderNames,
) {
    @get:JsonIgnore
    val isDisabled: Boolean
        get() = config.serviceDisabled

    @get:JsonIgnore
    val isEnabled: Boolean
        get() = !isDisabled

    val pmsApplicationName: String
        get() = "$cloudServiceName.${pmsRootQueueName.replace(PUBLIC_PRODUCTION_ROOT_QUEUE, DISCOVERY_QUEUE_NAME)}"

    fun isSharded(): Boolean {
        return config is ShardedServiceConfig
    }

    fun cloudQueueId(environment: ServiceEnvironment): String {
        return environment(environment).cloudQueueId
    }

    fun supportedEnvironments(): List<ServiceEnvironment> {
        return ServiceEnvironment.entries.filter(::supports)
    }

    fun distinctOnecloudSubqueues(): List<String> {
        return onecloudSubqueues.distinct()
    }

    fun regularOnecloudSubqueue(defaultSubqueueName: String = DEFAULT_ONECLOUD_SUBQUEUE): String {
        val resolvedSubqueues = distinctOnecloudSubqueues()
        return when (resolvedSubqueues.size) {
            0 -> defaultSubqueueName
            1 -> resolvedSubqueues.single()
            else -> error(
                "Expected single onecloud subqueue for non-sharded service $cloudServiceName, " +
                    "but got ${resolvedSubqueues.joinToString()}",
            )
        }
    }

    fun requireShardedOnecloudSubqueues(shardCount: Int): List<String> {
        val resolvedSubqueues = distinctOnecloudSubqueues()
        if (resolvedSubqueues.size != shardCount) {
            error(
                "Expected $shardCount onecloud subqueues for sharded service $cloudServiceName, " +
                    "but got ${resolvedSubqueues.joinToString()}",
            )
        }
        return resolvedSubqueues
    }

    fun environment(environment: ServiceEnvironment): ServiceEnvironmentRuntime {
        return when (environment) {
            ServiceEnvironment.PRODUCTION -> ServiceEnvironmentRuntime(
                environment = environment,
                cloudServiceName = cloudServiceName,
                applicationSecretDirectoryName = cloudServiceName,
                rootQueueName = serviceProductionRootQueueName,
            )

            ServiceEnvironment.CANARY -> ServiceEnvironmentRuntime(
                environment = environment,
                cloudServiceName = environment.applyPrefix(cloudServiceName),
                applicationSecretDirectoryName = cloudServiceName,
                rootQueueName = serviceProductionRootQueueName,
            )

            ServiceEnvironment.TESTING -> ServiceEnvironmentRuntime(
                environment = environment,
                cloudServiceName = environment.applyPrefix(cloudServiceName),
                applicationSecretDirectoryName = cloudServiceName,
                rootQueueName = serviceTestingRootQueueName,
            )
        }
    }

    fun supports(environment: ServiceEnvironment): Boolean {
        return when (environment) {
            ServiceEnvironment.PRODUCTION -> true
            ServiceEnvironment.CANARY -> hasCanary && serviceProductionRootQueueName.isNotBlank()
            ServiceEnvironment.TESTING -> hasTesting && serviceTestingRootQueueName.isNotBlank()
        }
    }

    companion object {
        private const val DEFAULT_ONECLOUD_SUBQUEUE = "java"
        private const val PUBLIC_PRODUCTION_ROOT_QUEUE = "public.app.production.recommender.prod"
        private const val DISCOVERY_QUEUE_NAME = "discovery"

        fun create(
            recommenderRuntime: RecommenderRuntime,
            service: RecomService<*>,
            serviceConfig: RecommenderServiceConfig,
        ): ServiceRuntime {
            val scope = service.resolveServiceScope(serviceConfig)
            val namespace = service.resolveNamespaceName(recommenderRuntime, serviceConfig)
            val cloudServiceName = service.resolveCloudServiceName(recommenderRuntime, serviceConfig)
            val queueSettings = service.resolveQueueSettings(recommenderRuntime, serviceConfig)

            return ServiceRuntime(
                type = service.type,
                config = serviceConfig,
                namespace = namespace,
                serviceName = service.serviceName,
                cloudServiceName = cloudServiceName,
                onecloudDirectoryName = service.resolveOnecloudDirectoryName(recommenderRuntime, serviceConfig),
                teamcityProject = service.resolveTeamcityProject(recommenderRuntime, serviceConfig),
                sourceDirectory = service.resolveSourceDirectory(recommenderRuntime, serviceConfig),
                gradleBuildCommand = service.resolveGradleBuildCommand(recommenderRuntime, serviceConfig),
                serviceProductionRootQueueName = queueSettings.rootQueueNames.productionRootQueueName,
                serviceTestingRootQueueName = queueSettings.rootQueueNames.testingRootQueueName,
                pmsRootQueueName = queueSettings.pmsRootQueueName,
                scope = scope,
                tenant = resolveTenant(serviceConfig, recommenderRuntime.recommenderName),
                onecloudSubqueues = queueSettings.onecloudSubqueues,
                hasTesting = service.hasTesting,
                hasCanary = service.hasCanary,
                hasSnapshots = serviceConfig.hasSnapshots,
                hasVector = serviceConfig.hasVector,
                names = parseNames(namespace),
            )
        }

        private fun resolveTenant(
            serviceConfig: RecommenderServiceConfig,
            recommenderName: String,
        ): String? {
            val tenantServiceConfig = serviceConfig as? TenantServiceConfig ?: return null
            return tenantServiceConfig.tenant ?: recommenderName
        }
    }
}

fun List<ServiceRuntime>.enabledServices(): List<ServiceRuntime> {
    return filter(ServiceRuntime::isEnabled)
}
