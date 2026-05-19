package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudDeploymentTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudQueueSubmission
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudServiceSubmission
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudServiceWaitTarget
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudStorageSubmission
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudSubmitQueuesTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudSubmitServicesTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudSubmitStoragesTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudWaitServicesRunningTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.OnecloudEnvironmentResources
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.OnecloudEnvironmentSetup
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.uniqueProductionDatacenters
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.RuntimeTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.enabledServices
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.context.TeamcityProjectsTaskContext

class OnecloudDeploymentContextBuilder(
    private val objectMapper: ObjectMapper,
    private val serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
) {

    fun build(runtimeContext: RuntimeTaskContext): OnecloudDeploymentTaskContext {
        val enabledServices = runtimeContext.services.enabledServices()
        val servicePlans = enabledServices.mapNotNull { serviceRuntime ->
            buildServicePlan(
                service = serviceRuntimeDefinitionResolver.service(serviceRuntime),
                runtimeContext = runtimeContext,
                serviceRuntime = serviceRuntime,
            )
        }

        val queueSubmissions = mutableListOf<OnecloudQueueSubmission>()
        val storageSubmissions = mutableListOf<OnecloudStorageSubmission>()
        val serviceSubmissions = mutableListOf<OnecloudServiceSubmission>()
        val serviceTargets = mutableListOf<OnecloudServiceWaitTarget>()

        if (servicePlans.isNotEmpty()) {
            queueSubmissions += buildRootQueueSubmissions(runtimeContext)
        }

        servicePlans.forEach { plan ->
            val environmentSetups = buildEnvironmentSetups(plan.serviceRuntime, runtimeContext.dcSettings)
            queueSubmissions += buildServiceQueueSubmissions(environmentSetups)

            plan.serviceRuntime.distinctOnecloudSubqueues().forEach { subqueueName ->
                val manifestSpec = buildSubqueueManifestSpec(
                    baseServiceManifestSpec = plan.manifestSpec,
                    serviceRuntime = plan.serviceRuntime,
                    subqueueName = subqueueName,
                )

                environmentSetups.forEach { environmentSetup ->
                    val resources = OnecloudEnvironmentBuilder.buildEnvironmentResources(
                        environment = environmentSetup.environment,
                        serviceManifestSpec = manifestSpec,
                    )
                    val dc = environmentSetup.datacenterCode.toDatacenterCode()

                    storageSubmissions += buildStorageSubmission(
                        environmentSetup = environmentSetup,
                        subqueueName = subqueueName,
                        manifestSpec = manifestSpec,
                        resources = resources,
                        dc = dc,
                    )
                    serviceSubmissions += buildServiceSubmission(
                        environmentSetup = environmentSetup,
                        subqueueName = subqueueName,
                        serviceRuntime = plan.serviceRuntime,
                        manifestSpec = manifestSpec,
                        resources = resources,
                        dc = dc,
                    )
                    serviceTargets += OnecloudServiceWaitTarget(
                        name = fullOnecloudObjectName(
                            queue = environmentSetup.queueId,
                            name = subqueueName,
                        ),
                        dcs = listOf(dc),
                    )
                }
            }
        }

        return OnecloudDeploymentTaskContext(
            teamcityProjectsTaskContext = buildTeamcityContext(runtimeContext, enabledServices),
            onecloudSubmitQueuesTaskContext = OnecloudSubmitQueuesTaskContext(queueSubmissions.mergeQueueDcs()),
            onecloudSubmitStoragesTaskContext = OnecloudSubmitStoragesTaskContext(storageSubmissions.mergeStorageDcs()),
            onecloudSubmitServicesTaskContext = OnecloudSubmitServicesTaskContext(serviceSubmissions.mergeServiceDcs()),
            onecloudWaitServicesRunningTaskContext = OnecloudWaitServicesRunningTaskContext(serviceTargets.mergeTargetDcs()),
        )
    }

    private fun buildTeamcityContext(
        runtimeContext: RuntimeTaskContext,
        enabledServices: List<ServiceRuntime>,
    ): TeamcityProjectsTaskContext {
        return TeamcityProjectsTaskContext(
            recommenderName = runtimeContext.recommenderName,
            recommenderClassName = runtimeContext.recommenderClassName,
            projectName = runtimeContext.projectName,
            services = enabledServices,
            dcSettings = runtimeContext.dcSettings,
            branch = runtimeContext.branch,
            teamsChatId = runtimeContext.teamsChatId.orEmpty(),
        )
    }

    private fun buildRootQueueSubmissions(runtimeContext: RuntimeTaskContext): List<OnecloudQueueSubmission> {
        return runtimeContext.recommender.rootQueueGroups().flatMap { rootQueueGroup ->
            buildList {
                buildQueueSubmission(
                    queueName = rootQueueGroup.productionRootQueueName,
                    productId = rootQueueGroup.productId,
                    dcs = runtimeContext.dcSettings.uniqueProductionDatacenters().toDatacenterCodes(),
                )?.let(::add)

                rootQueueGroup.testingRootQueueName?.let { testingRootQueueName ->
                    buildQueueSubmission(
                        queueName = testingRootQueueName,
                        productId = rootQueueGroup.productId,
                        dcs = runtimeContext.dcSettings.testingDcs.toDatacenterCodes(),
                    )?.let(::add)
                }
            }
        }
    }

    private fun buildServiceQueueSubmissions(
        environmentSetups: List<OnecloudEnvironmentSetup>,
    ): List<OnecloudQueueSubmission> {
        return environmentSetups.mapNotNull { environmentSetup ->
            buildQueueSubmission(
                queueName = environmentSetup.queueId,
                dcs = listOf(environmentSetup.datacenterCode.toDatacenterCode()),
            )
        }
    }

    private fun buildQueueSubmission(
        queueName: String,
        productId: Int? = null,
        dcs: List<DatacenterCode>,
    ): OnecloudQueueSubmission? {
        val targetDcs = dcs.distinctByName()
        if (targetDcs.isEmpty()) {
            return null
        }

        return OnecloudQueueSubmission(
            queueJson = toJson(
                linkedMapOf<String, Any>(
                    "type" to "queue",
                    "namespace" to ONECLOUD_NAMESPACE,
                    "name" to queueName,
                    "state" to "RUNNING",
                    "quota" to "unlimited",
                ).apply {
                    productId?.let { put("meta", linkedMapOf("product" to it.toString())) }
                },
            ),
            dcs = targetDcs,
        )
    }

    private fun buildStorageSubmission(
        environmentSetup: OnecloudEnvironmentSetup,
        subqueueName: String,
        manifestSpec: ServiceManifestSpec,
        resources: OnecloudEnvironmentResources,
        dc: DatacenterCode,
    ): OnecloudStorageSubmission {
        return OnecloudStorageSubmission(
            storageJson = toJson(
                linkedMapOf(
                    "type" to "storage",
                    "namespace" to ONECLOUD_NAMESPACE,
                    "name" to subqueueName,
                    "queue" to environmentSetup.queueId,
                    "availability" to buildAvailability(resources),
                    "prealloc" to linkedMapOf(
                        "vcores" to resources.allocatedCpuCores.toString(),
                        "mem" to manifestSpec.cloudRam,
                        "lan_out" to resources.cloudLanOut,
                        "lan_in" to resources.cloudLanIn,
                    ),
                    "shards" to resources.cloudReplicas.toString(),
                    "filter" to ONECLOUD_FILTER,
                    "volumes" to linkedMapOf(
                        "ephemeral" to linkedMapOf(
                            "size" to manifestSpec.cloudVolumeSize,
                            "durability" to "cache",
                            "type" to manifestSpec.cloudVolumeType,
                        ),
                    ),
                    "comment" to "",
                ),
            ),
            queue = environmentSetup.queueId,
            shards = resources.cloudReplicas,
            dcs = listOf(dc),
        )
    }

    private fun buildServiceSubmission(
        environmentSetup: OnecloudEnvironmentSetup,
        subqueueName: String,
        serviceRuntime: ServiceRuntime,
        manifestSpec: ServiceManifestSpec,
        resources: OnecloudEnvironmentResources,
        dc: DatacenterCode,
    ): OnecloudServiceSubmission {
        return OnecloudServiceSubmission(
            serviceJson = toJson(
                buildServiceManifest(
                    environmentSetup = environmentSetup,
                    subqueueName = subqueueName,
                    serviceRuntime = serviceRuntime,
                    manifestSpec = manifestSpec,
                    resources = resources,
                ),
            ),
            queue = environmentSetup.queueId,
            replicas = resources.cloudReplicas.toString(),
            minRunning = resources.cloudAvailability,
            pause = resources.cloudPause,
            dcs = listOf(dc),
        )
    }

    private fun buildServiceManifest(
        environmentSetup: OnecloudEnvironmentSetup,
        subqueueName: String,
        serviceRuntime: ServiceRuntime,
        manifestSpec: ServiceManifestSpec,
        resources: OnecloudEnvironmentResources,
    ): Map<String, Any?> {
        val resourceAllocation = buildServiceResourceAllocation(
            serviceManifestSpec = manifestSpec,
            environment = environmentSetup.environment,
            allocatedCpuCores = resources.allocatedCpuCores,
        )
        val environmentVariables = OnecloudEnvironmentBuilder.buildEnvironmentVariables(
            serviceRuntime = serviceRuntime,
            serviceManifestSpec = manifestSpec,
            environment = environmentSetup.environment,
            allocatedCpuCores = resources.allocatedCpuCores,
        )
        val manifest = linkedMapOf(
            "type" to "service",
            "namespace" to ONECLOUD_NAMESPACE,
            "name" to subqueueName,
            "queue" to environmentSetup.queueId,
            "comment" to null,
            "availability" to buildAvailability(resources),
            "filter" to ONECLOUD_FILTER,
            "alloc" to linkedMapOf(
                "vcores" to resourceAllocation.cpuVcores,
                "mem" to resourceAllocation.cloudRam,
                "lan_out" to resources.cloudLanOut,
                "lan_in" to resources.cloudLanIn,
            ),
            "env" to environmentVariables.map { (variableName, variableValue) -> "$variableName=$variableValue" },
            "image" to linkedMapOf(
                "registry" to "registry.nda.example.invalid",
                "name" to manifestSpec.imageName,
                "version" to manifestSpec.imageVersion,
                "login" to "public",
            ),
            "mounts" to linkedMapOf("ephemeral" to "/ephemeral"),
            "timeouts" to linkedMapOf(
                "deploy" to "5m",
                "start" to "15m",
                "stop" to "15m",
            ),
            "replicas" to resources.cloudReplicas,
            "network" to linkedMapOf(
                "wan" to manifestSpec.wanNetworks.joinToString(","),
                "wlan" to manifestSpec.wlanNetworks.joinToString(","),
            ),
            "ports" to buildPorts(manifestSpec),
        )

        manifestSpec.startAttemptsLimit?.let { manifest["startAttemptsLimit"] = it }
        OnecloudEnvironmentBuilder.buildSidecars(
            serviceRuntime = serviceRuntime,
            environment = environmentSetup.environment,
            serviceManifestSpec = manifestSpec,
        ).takeIf { sidecars -> sidecars.isNotEmpty() }
            ?.let { sidecars -> manifest["sidecars"] = sidecars }

        return manifest
    }

    private fun buildAvailability(resources: OnecloudEnvironmentResources): Map<String, Any> {
        return linkedMapOf<String, Any>(
            "minRunning" to resources.cloudAvailability,
        ).apply {
            resources.cloudPause?.let { put("pause", it) }
            put("governor", "reported")
        }
    }

    private fun buildPorts(manifestSpec: ServiceManifestSpec): Map<String, String> {
        return linkedMapOf<String, String>().apply {
            (manifestSpec.ports + manifestSpec.extraPorts)
                .distinct()
                .forEach { exposedPort ->
                    put(
                        exposedPort,
                        if (manifestSpec.ports.contains(exposedPort)) {
                            "lan,wan,tcp,started"
                        } else {
                            "lan,wan,tcp"
                        },
                    )
                }
        }
    }

    private fun buildEnvironmentSetups(
        serviceRuntime: ServiceRuntime,
        dcSettings: RecommenderDcSettings,
    ): List<OnecloudEnvironmentSetup> {
        val supportedEnvironments = serviceRuntime.supportedEnvironments().toSet()
        return buildList {
            dcSettings.productionDcs.forEach { datacenterCode ->
                add(
                    OnecloudEnvironmentSetup(
                        environment = ServiceEnvironment.PRODUCTION,
                        datacenterCode = datacenterCode,
                        queueId = serviceRuntime.cloudQueueId(ServiceEnvironment.PRODUCTION),
                    ),
                )
            }

            if (ServiceEnvironment.CANARY in supportedEnvironments) {
                dcSettings.canaryDcs.forEach { datacenterCode ->
                    add(
                        OnecloudEnvironmentSetup(
                            environment = ServiceEnvironment.CANARY,
                            datacenterCode = datacenterCode,
                            queueId = serviceRuntime.cloudQueueId(ServiceEnvironment.CANARY),
                        ),
                    )
                }
            }

            if (ServiceEnvironment.TESTING in supportedEnvironments) {
                dcSettings.testingDcs.forEach { datacenterCode ->
                    add(
                        OnecloudEnvironmentSetup(
                            environment = ServiceEnvironment.TESTING,
                            datacenterCode = datacenterCode,
                            queueId = serviceRuntime.cloudQueueId(ServiceEnvironment.TESTING),
                        ),
                    )
                }
            }
        }
    }

    private fun buildSubqueueManifestSpec(
        baseServiceManifestSpec: ServiceManifestSpec,
        serviceRuntime: ServiceRuntime,
        subqueueName: String,
    ): ServiceManifestSpec {
        val shardEnvironmentVariables = if (serviceRuntime.isSharded()) {
            mapOf("SHARD" to subqueueName.removeSuffix("-java").replace("shard", "shard-"))
        } else {
            emptyMap()
        }

        return baseServiceManifestSpec.copy(
            additionalEnv = baseServiceManifestSpec.additionalEnv + shardEnvironmentVariables,
            additionalTestingEnv = baseServiceManifestSpec.additionalTestingEnv + shardEnvironmentVariables,
        )
    }

    private fun buildServiceResourceAllocation(
        serviceManifestSpec: ServiceManifestSpec,
        environment: ServiceEnvironment,
        allocatedCpuCores: Int,
    ): ServiceResourceAllocation {
        var cloudRam = serviceManifestSpec.cloudRam
        var cpuVcores = allocatedCpuCores.toDouble()
        if (serviceManifestSpec.enableHermesSidecar) {
            cpuVcores -= 0.5
            cloudRam -= 1
        }
        if (
            serviceManifestSpec.enableVectorSidecar &&
            environment in listOf(ServiceEnvironment.CANARY, ServiceEnvironment.PRODUCTION)
        ) {
            cpuVcores -= 0.4
            cloudRam -= 1
        }
        if (cpuVcores <= 1.0 || cloudRam <= 1.0) {
            error("Too low alloc vcores:$cpuVcores, mem:$cloudRam")
        }

        return ServiceResourceAllocation(
            cpuVcores = cpuVcores.toString(),
            cloudRam = cloudRam,
        )
    }

    private fun <TConfig : RecommenderServiceConfig> buildServicePlan(
        service: RecomService<TConfig>,
        runtimeContext: RuntimeTaskContext,
        serviceRuntime: ServiceRuntime,
    ): OnecloudServicePlan? {
        val serviceConfig = serviceRuntime.config.requireConfig(service.configClass)
        if (!service.shouldGenerateOnecloudManifests(serviceRuntime, serviceConfig)) {
            return null
        }

        return OnecloudServicePlan(
            serviceRuntime = serviceRuntime,
            manifestSpec = service.buildOnecloudManifest(
                recommenderRuntime = runtimeContext.recommender,
                createMinOneCloudConfiguration = runtimeContext.recommender.createMinOneCloudConfiguration,
                dcSettings = runtimeContext.dcSettings,
                serviceRuntime = serviceRuntime,
                serviceConfig = serviceConfig,
            ),
        )
    }

    private fun List<OnecloudQueueSubmission>.mergeQueueDcs(): List<OnecloudQueueSubmission> {
        return groupBy { submission -> submission.copy(dcs = emptyList()) }
            .map { (submission, groupedSubmissions) ->
                submission.copy(dcs = groupedSubmissions.flatMap { it.dcs }.distinctByName())
            }
    }

    private fun List<OnecloudStorageSubmission>.mergeStorageDcs(): List<OnecloudStorageSubmission> {
        return groupBy { submission -> submission.copy(dcs = emptyList()) }
            .map { (submission, groupedSubmissions) ->
                submission.copy(dcs = groupedSubmissions.flatMap { it.dcs }.distinctByName())
            }
    }

    private fun List<OnecloudServiceSubmission>.mergeServiceDcs(): List<OnecloudServiceSubmission> {
        return groupBy { submission -> submission.copy(dcs = emptyList()) }
            .map { (submission, groupedSubmissions) ->
                submission.copy(dcs = groupedSubmissions.flatMap { it.dcs }.distinctByName())
            }
    }

    private fun List<OnecloudServiceWaitTarget>.mergeTargetDcs(): List<OnecloudServiceWaitTarget> {
        return groupBy { target -> target.copy(dcs = emptyList()) }
            .map { (target, groupedTargets) ->
                target.copy(dcs = groupedTargets.flatMap { it.dcs }.distinctByName())
            }
    }

    private fun String.toDatacenterCode(): DatacenterCode {
        return DatacenterCode.fromValue(this)
    }

    private fun List<String>.toDatacenterCodes(): List<DatacenterCode> {
        return map { datacenterCode -> datacenterCode.toDatacenterCode() }.distinctByName()
    }

    private fun List<DatacenterCode>.distinctByName(): List<DatacenterCode> {
        return distinct().sortedBy { datacenterCode -> datacenterCode.name }
    }

    private fun fullOnecloudObjectName(
        queue: String,
        name: String,
    ): String {
        return "$queue/$name"
    }

    private fun toJson(manifest: Map<String, Any?>): String {
        return objectMapper.writeValueAsString(manifest)
    }

    private data class OnecloudServicePlan(
        val serviceRuntime: ServiceRuntime,
        val manifestSpec: ServiceManifestSpec,
    )

    private data class ServiceResourceAllocation(
        val cpuVcores: String,
        val cloudRam: Int,
    )

    private companion object {
        private const val ONECLOUD_NAMESPACE = "public"
        private const val ONECLOUD_FILTER = """has("ipv6", "on") && has("cpu_flag_avx","on")"""
    }
}
