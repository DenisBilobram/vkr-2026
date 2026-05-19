package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.mr

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabFile
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ShardedServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.client.ServicehostAdminClient
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.context.ServicehostTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.Mesh
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostBackendConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostBackendTestsPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostConfigApplyResult
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostEncodedEntry
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphEmbedNodeConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphNames
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphNodeConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphShardedNodeConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostGraphTransparentNodeConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostRequestPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostResourceType
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.cluster.ServicehostClusterNameResolver
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.path.ServicehostPathResolver
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostApiPayloadWriter
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostBackendWriter
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostGraphWriter
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostRoutingWriter
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostShootingTestsWriter
import java.nio.file.Path

class ServicehostVerticalMrService(
    private val servicehostBackendWriter: ServicehostBackendWriter,
    private val servicehostRoutingWriter: ServicehostRoutingWriter,
    private val servicehostGraphWriter: ServicehostGraphWriter,
    private val servicehostApiPayloadWriter: ServicehostApiPayloadWriter,
    private val servicehostShootingTestsWriter: ServicehostShootingTestsWriter,
    private val servicehostAdminClient: ServicehostAdminClient,
) {

    fun createVerticalMr(taskContext: ServicehostTaskContext): ServicehostConfigApplyResult? {
        val generationContext = resolveGenerationContext(taskContext)
        val requiredServiceRuntimes = resolveOnlineServiceRuntimes(taskContext.services) ?: return null
        val graphGenerationPlan = buildOnlineGraphGenerationPlan(
            taskContext = taskContext,
            graphNames = generationContext.graphNames,
            requiredServiceRuntimes = requiredServiceRuntimes,
        )
//        val appendGraphShootingTests = appendGraphShootingTests(taskContext.workspaceRoot, graphGenerationPlan.shootingTestGraphNames)
        return applyVerticalConfig(
            taskContext = taskContext,
            generationContext = generationContext,
            graphGenerationPlan = graphGenerationPlan,
            backendGenerationPlan = buildOnlineBackendGenerationPlan(taskContext),
            servicehostEnvironments = ALL_SERVICEHOST_ENVIRONMENTS,
        )
    }

    fun createOfflineVerticalMr(taskContext: ServicehostTaskContext): ServicehostConfigApplyResult? {
        val generationContext = resolveGenerationContext(taskContext)
        val requiredServiceRuntimes = resolveOfflineServiceRuntimes(taskContext.services) ?: return null

        return applyVerticalConfig(
            taskContext = taskContext,
            generationContext = generationContext,
            graphGenerationPlan = buildOfflineGraphGenerationPlan(
                taskContext = taskContext,
                graphNames = generationContext.graphNames,
                requiredServiceRuntimes = requiredServiceRuntimes,
            ),
            backendGenerationPlan = buildOfflineBackendGenerationPlan(taskContext),
            servicehostEnvironments = PRODUCTION_ONLY_SERVICEHOST_ENVIRONMENTS,
            artifactSuffix = OFFLINE_PAYLOAD_ARTIFACT_SUFFIX,
        )
    }

    private fun applyVerticalConfig(
        taskContext: ServicehostTaskContext,
        generationContext: ServicehostGenerationContext,
        graphGenerationPlan: GraphGenerationPlan,
        backendGenerationPlan: BackendGenerationPlan,
        servicehostEnvironments: Set<ServiceEnvironment>,
        artifactSuffix: String = "",
    ): ServicehostConfigApplyResult {
        val servicehostRequestPayload = initializeServicehostRequestPayload(taskContext.servicehostClusterName)
        val backendTestsPayload = ServicehostBackendTestsPayload()
        val serviceNamesByEnvironment = initializeServiceNamesByEnvironment()

        generateBackendMeshConfigs(
            taskContext = taskContext,
            servicehostRootDirectory = generationContext.servicehostRootDirectory,
            backendGenerationPlan = backendGenerationPlan,
            servicehostRequestPayload = servicehostRequestPayload,
            serviceNamesByEnvironment = serviceNamesByEnvironment,
            servicehostEnvironments = servicehostEnvironments,
        )

        graphGenerationPlan.entryGraphName?.let { entryGraphName ->
            servicehostRoutingWriter.writeRoutingClusterFiles(
                servicehostRootDirectory = generationContext.servicehostRootDirectory,
                fullClusterName = generationContext.fullClusterName,
                entryGraphName = entryGraphName,
                serviceNamesByEnvironment = serviceNamesByEnvironment,
                servicehostEnvironments = servicehostEnvironments,
            )
        }

        servicehostGraphWriter.writeGraphConfigs(
            servicehostRootDirectory = generationContext.servicehostRootDirectory,
            recommenderName = generationContext.recommenderName,
            graphConfigs = graphGenerationPlan.graphConfigs,
            servicehostRequestPayload = servicehostRequestPayload,
            backendTestsPayload = backendTestsPayload,
        )

        servicehostApiPayloadWriter.writeVerticalPayloads(
            servicehostRootDirectory = generationContext.servicehostRootDirectory,
            servicehostRequestPayload = servicehostRequestPayload,
            backendTestsPayload = backendTestsPayload,
            fullClusterName = generationContext.fullClusterName,
            artifactSuffix = artifactSuffix,
        )

        return servicehostAdminClient.applyClusterConfig(
            fullClusterName = generationContext.fullClusterName,
            servicehostRequestPayload = servicehostRequestPayload,
        )
    }

    private fun resolveGenerationContext(taskContext: ServicehostTaskContext): ServicehostGenerationContext {
        val recommenderName = taskContext.recommenderName
        val clusterNames = ServicehostClusterNameResolver.resolveClusterNames(
            recommenderName = recommenderName,
            servicehostClusterName = taskContext.servicehostClusterName,
        )

        return ServicehostGenerationContext(
            recommenderName = recommenderName,
            servicehostRootDirectory = ServicehostPathResolver.resolveServicehostRootDirectory(
                workspaceRoot = taskContext.workspaceRoot,
                recommenderName = recommenderName,
            ),
            fullClusterName = clusterNames.fullClusterName,
            graphNames = buildGraphNames(recommenderName),
        )
    }

    private fun buildGraphNames(recommenderName: String): ServicehostGraphNames {
        return ServicehostGraphNames(
            exportGraphName = "export-$recommenderName",
            recommendGraphName = "recommend-$recommenderName",
            recommendBaseGraphName = "recommend-$recommenderName-base",
            storagesGraphName = "storages-$recommenderName",
            offlineGraphName = "$recommenderName-i2i",
            offlineRecommendGraphName = "$recommenderName-i2i-recommender",
            offlineBaseGraphName = "$recommenderName-i2i-recommender-base",
        )
    }

    private fun appendGraphShootingTests(workspaceRoot: Path, graphNames: List<String>): GitlabFile {
        return servicehostShootingTestsWriter.appendGraphTests(
            workspaceRoot = workspaceRoot,
            graphNames = graphNames,
        )
    }

    private fun initializeServicehostRequestPayload(cluster: String?): ServicehostRequestPayload {
        val graphs = mutableListOf<ServicehostEncodedEntry>()
        val backends = mutableListOf(ServicehostEncodedEntry(name = "__publication_placeholder__", type = null, content = "Cg=="))

        if (cluster == null || !servicehostAdminClient.clusterExist(cluster)) {
            throw RuntimeException("Cluster $cluster not found")
        }

        servicehostAdminClient.getGraphs(cluster).items
            .filter {
                it.name == "routes" && it.typeEquals(ServicehostResourceType.ROUTE)
            }.forEach {
                val file = servicehostAdminClient.getGraph(cluster, it.name)
                graphs.add(ServicehostEncodedEntry(file.name.replace(".yaml", ""), null, file.content))
            }

        val backendConfigNames =
            Mesh.allMesh.map { mesh -> mesh.replace("/mesh", "") }.toCollection(mutableListOf())

        servicehostAdminClient.getBackends(cluster).items
            .filter {
                it.name in backendConfigNames && it.typeEquals(ServicehostResourceType.CLUSTER_MESH)
            }.forEach {
                val file = servicehostAdminClient.getBackend(cluster, it.name)
                backends.add(
                    ServicehostEncodedEntry(
                        file.name.replace(".yaml", ""),
                        it.type,
                        file.content
                    )
                )
            }

        graphs.find { it.name == "routes" }
            ?: println("СТАРАЯ КОНФИГУРАЦИЯ КЛАСТЕРА, ПРИДЕТСЯ КЛАСТЬ РУКАМИ routing_config.json и servicehost.conf")

        return ServicehostRequestPayload(backends = backends, graphs = graphs)
    }

    private fun initializeServiceNamesByEnvironment(): MutableMap<String, MutableList<String>> {
        return ServiceEnvironment.entries.associateTo(mutableMapOf()) { environment ->
            environment.id to mutableListOf()
        }
    }

    private fun buildOnlineBackendGenerationPlan(taskContext: ServicehostTaskContext): BackendGenerationPlan {
        return buildBackendGenerationPlan(
            serviceRuntimes = taskContext.services.filter { serviceRuntime -> serviceRuntime.type in ONLINE_SERVICE_TYPES },
        )
    }

    private fun buildOfflineBackendGenerationPlan(taskContext: ServicehostTaskContext): BackendGenerationPlan {
        return buildBackendGenerationPlan(
            serviceRuntimes = taskContext.services.filter { serviceRuntime -> serviceRuntime.type in OFFLINE_SERVICE_TYPES },
        )
    }

    private fun buildBackendGenerationPlan(
        serviceRuntimes: List<ServiceRuntime>,
    ): BackendGenerationPlan {
        val regularBackends = serviceRuntimes
            .filter { !it.isSharded() && it.scope != ServiceScope.PROJECT_SCOPED }
        val sharedBackends = serviceRuntimes
            .filter { !it.isSharded() && it.scope == ServiceScope.PROJECT_SCOPED }
        val allRegularBackends = regularBackends + sharedBackends

        val shardedBackends = serviceRuntimes
            .filter(ServiceRuntime::isSharded)
            .map { serviceRuntime ->
                ShardedBackendGeneration(
                    serviceRuntime = serviceRuntime,
                    shardCount = requireShardsCount(serviceRuntime),
                )
            }

        return BackendGenerationPlan(
            regularBackends = allRegularBackends,
            shardedBackends = shardedBackends,
        )
    }

    private fun requireShardsCount(serviceRuntime: ServiceRuntime): Int {
        return serviceRuntime.config.requireConfig(ShardedServiceConfig::class).shardsCount
    }

    private fun generateBackendMeshConfigs(
        taskContext: ServicehostTaskContext,
        servicehostRootDirectory: Path,
        backendGenerationPlan: BackendGenerationPlan,
        servicehostRequestPayload: ServicehostRequestPayload,
        serviceNamesByEnvironment: MutableMap<String, MutableList<String>>,
        servicehostEnvironments: Set<ServiceEnvironment>,
    ) {
        backendGenerationPlan.regularBackends.forEach { serviceRuntime ->
            val queueName = serviceRuntime.cloudServiceName
            writeBackendMeshConfigForQueue(
                taskContext = taskContext,
                serviceRuntime = serviceRuntime,
                servicehostRootDirectory = servicehostRootDirectory,
                queueName = queueName,
                backendConfigs = listOf(
                    ServicehostBackendConfig(
                        backendName = queueName,
                        onecloudShardName = serviceRuntime.regularOnecloudSubqueue(),
                    ),
                ),
                servicehostRequestPayload = servicehostRequestPayload,
                serviceNamesByEnvironment = serviceNamesByEnvironment,
                servicehostEnvironments = servicehostEnvironments,
            )
        }

        backendGenerationPlan.shardedBackends.forEach { shardedBackend ->
            val queueName = shardedBackend.serviceRuntime.cloudServiceName
            val backendConfigs = shardedBackend.serviceRuntime
                .requireShardedOnecloudSubqueues(shardedBackend.shardCount)
                .mapIndexed { shardIndex, onecloudSubqueueName ->
                ServicehostBackendConfig(
                    backendName = "$queueName-shard$shardIndex",
                    onecloudShardName = onecloudSubqueueName,
                )
            }
            writeBackendMeshConfigForQueue(
                taskContext = taskContext,
                serviceRuntime = shardedBackend.serviceRuntime,
                servicehostRootDirectory = servicehostRootDirectory,
                queueName = queueName,
                backendConfigs = backendConfigs,
                servicehostRequestPayload = servicehostRequestPayload,
                serviceNamesByEnvironment = serviceNamesByEnvironment,
                servicehostEnvironments = servicehostEnvironments,
            )
        }
    }

    private fun writeBackendMeshConfigForQueue(
        taskContext: ServicehostTaskContext,
        serviceRuntime: ServiceRuntime,
        servicehostRootDirectory: Path,
        queueName: String,
        backendConfigs: List<ServicehostBackendConfig>,
        servicehostRequestPayload: ServicehostRequestPayload,
        serviceNamesByEnvironment: MutableMap<String, MutableList<String>>,
        servicehostEnvironments: Set<ServiceEnvironment>,
    ) {
        servicehostBackendWriter.writeBackendMeshConfigs(
            taskContext = taskContext,
            serviceRuntime = serviceRuntime,
            servicehostRootDirectory = servicehostRootDirectory,
            queueName = queueName,
            backendConfigs = backendConfigs,
            servicehostRequestPayload = servicehostRequestPayload,
            serviceNamesByEnvironment = serviceNamesByEnvironment,
            servicehostEnvironments = servicehostEnvironments,
        )
    }

    private fun buildOnlineGraphGenerationPlan(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        requiredServiceRuntimes: OnlineServiceRuntimes,
    ): GraphGenerationPlan {
        return GraphGenerationPlan(
            graphConfigs = buildOnlineGraphConfigs(
                taskContext = taskContext,
                graphNames = graphNames,
                requiredServiceRuntimes = requiredServiceRuntimes,
            ),
            shootingTestGraphNames = listOf(
                graphNames.exportGraphName,
                graphNames.recommendGraphName,
                graphNames.storagesGraphName,
            ),
            entryGraphName = graphNames.exportGraphName,
        )
    }

    private fun buildOfflineGraphGenerationPlan(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        requiredServiceRuntimes: OfflineServiceRuntimes,
    ): GraphGenerationPlan {
        return GraphGenerationPlan(
            graphConfigs = buildOfflineGraphConfigs(
                taskContext = taskContext,
                graphNames = graphNames,
                requiredServiceRuntimes = requiredServiceRuntimes,
            ),
            shootingTestGraphNames = listOf(
                graphNames.offlineGraphName,
                graphNames.offlineRecommendGraphName,
            ),
            entryGraphName = resolveOfflineEntryGraphName(
                taskContext = taskContext,
                graphNames = graphNames,
            ),
        )
    }

    private fun resolveOfflineEntryGraphName(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
    ): String? {
        return if (taskContext.services.none { serviceRuntime -> serviceRuntime.type in ONLINE_SERVICE_TYPES }) {
            graphNames.offlineGraphName
        } else {
            null
        }
    }

    private fun resolveOnlineServiceRuntimes(serviceRuntimes: List<ServiceRuntime>): OnlineServiceRuntimes? {
        if (serviceRuntimes.none { serviceRuntime -> serviceRuntime.type in ONLINE_SERVICE_TYPES }) {
            return null
        }

        val baseServiceRuntime = requireServiceRuntime(serviceRuntimes, ServiceType.BASE)
        val metaServiceRuntime = requireServiceRuntime(serviceRuntimes, ServiceType.META)
        val gatewayServiceRuntime = serviceRuntimes.firstOrNull { serviceRuntime ->
            serviceRuntime.type == ServiceType.GATEWAY || serviceRuntime.type == ServiceType.PLATFORM_GATEWAY
        } ?: error("Gateway service is required for servicehost generation")
        val mediatorServiceRuntime = requireServiceRuntime(serviceRuntimes, ServiceType.MEDIATOR)
        val ytProxyServiceRuntime = requireServiceRuntime(serviceRuntimes, ServiceType.YT_PROXY)

        return OnlineServiceRuntimes(
            baseServiceRuntime = baseServiceRuntime,
            metaServiceRuntime = metaServiceRuntime,
            gatewayServiceRuntime = gatewayServiceRuntime,
            mediatorServiceRuntime = mediatorServiceRuntime,
            ytProxyServiceRuntime = ytProxyServiceRuntime,
        )
    }

    private fun resolveOfflineServiceRuntimes(serviceRuntimes: List<ServiceRuntime>): OfflineServiceRuntimes? {
        if (serviceRuntimes.none { serviceRuntime -> serviceRuntime.type in OFFLINE_SERVICE_TYPES }) {
            return null
        }

        return OfflineServiceRuntimes(
            factorProxyServiceRuntime = requireServiceRuntime(serviceRuntimes, ServiceType.FACTOR_PROXY),
            metaI2IServiceRuntime = requireServiceRuntime(serviceRuntimes, ServiceType.META_I2I),
            baseI2IServiceRuntime = requireServiceRuntime(serviceRuntimes, ServiceType.BASE_I2I),
        )
    }

    private fun buildOnlineGraphConfigs(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        requiredServiceRuntimes: OnlineServiceRuntimes,
    ): List<ServicehostGraphConfig> {
        val recommenderName = taskContext.recommenderName
        val ytProxyBackendName = resolveYtProxyBackendName(requiredServiceRuntimes.ytProxyServiceRuntime)
        val ytProxyTenantName = requiredServiceRuntimes.ytProxyServiceRuntime.tenant ?: recommenderName

        return listOf(
            buildExportGraphConfig(
                taskContext = taskContext,
                graphNames = graphNames,
                mediatorServiceRuntime = requiredServiceRuntimes.mediatorServiceRuntime,
            ),
            buildRecommendGraphConfig(
                taskContext = taskContext,
                graphNames = graphNames,
                gatewayServiceRuntime = requiredServiceRuntimes.gatewayServiceRuntime,
                metaServiceRuntime = requiredServiceRuntimes.metaServiceRuntime,
            ),
            buildRecommendBaseGraphConfig(
                taskContext = taskContext,
                graphNames = graphNames,
                baseServiceRuntime = requiredServiceRuntimes.baseServiceRuntime,
            ),
            buildStoragesGraphConfig(
                serviceOwner = taskContext.serviceOwner,
                graphNames = graphNames,
                ytProxyServiceRuntime = requiredServiceRuntimes.ytProxyServiceRuntime,
                ytProxyBackendName = ytProxyBackendName,
                ytProxyTenantName = ytProxyTenantName,
            ),
        )
    }

    private fun buildOfflineGraphConfigs(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        requiredServiceRuntimes: OfflineServiceRuntimes,
    ): List<ServicehostGraphConfig> {
        return listOf(
            buildOfflineGraphConfig(
                taskContext = taskContext,
                graphNames = graphNames,
                factorProxyServiceRuntime = requiredServiceRuntimes.factorProxyServiceRuntime,
            ),
            buildOfflineRecommendGraphConfig(
                taskContext = taskContext,
                graphNames = graphNames,
                metaI2IServiceRuntime = requiredServiceRuntimes.metaI2IServiceRuntime,
            ),
            buildOfflineBaseGraphConfig(
                taskContext = taskContext,
                graphNames = graphNames,
                baseI2IServiceRuntime = requiredServiceRuntimes.baseI2IServiceRuntime,
            ),
        )
    }

    private fun buildExportGraphConfig(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        mediatorServiceRuntime: ServiceRuntime,
    ): ServicehostGraphConfig {
        val recommenderName = taskContext.recommenderName
        val mediatorBackendName = convertToMeshBackendName(mediatorServiceRuntime.cloudServiceName)

        return ServicehostGraphConfig(
            graphName = graphNames.exportGraphName,
            graphInputDeps = listOf("PROTO_HTTP_REQUEST"),
            graphOutputDeps = mapOf(
                "RESPONSE" to listOf(
                    "MEDIATOR_REQUEST@!proto_http_response",
                    "MEDIATOR_RESPONSE@!proto_http_response",
                ),
            ),
            responsibles = listOf(taskContext.serviceOwner),
            edgeExpressions = mapOf(
                "MEDIATOR_REQUEST->RESPONSE" to "MEDIATOR_REQUEST[use_cache_flow]",
                "MEDIATOR_RESPONSE->RESPONSE" to "!MEDIATOR_REQUEST[use_cache_flow]",
            ),
            graphEmbedNodes = listOf(togglesTenantNode(recommenderName)),
            graphNodes = listOf(
                masterTogglesNode(),
                ServicehostGraphNodeConfig(
                    nodeName = "MEDIATOR_REQUEST",
                    backendName = mediatorBackendName,
                    teamcityProject = mediatorServiceRuntime.teamcityProject,
                    nodeDependencies = listOf("PROTO_HTTP_REQUEST", "MASTER_TOGGLES"),
                    handler = "/apphost/mediator/on-request",
                    hardTimeout = "50ms",
                ),
                ServicehostGraphNodeConfig(
                    nodeName = "STORAGES",
                    backendName = "SELF",
                    nodeDependencies = listOf(
                        "!MEDIATOR_REQUEST->MEDIATOR@!user_profile_request->yt_proxy_user_data_request",
                    ),
                    handler = "/_subhost/${graphNames.storagesGraphName}",
                    hardTimeout = "100ms",
                ),
                ServicehostGraphNodeConfig(
                    nodeName = "RECOMMEND",
                    backendName = "SELF",
                    nodeDependencies = listOf("STORAGES", "COMMON_REQUEST_CONTEXT"),
                    handler = "/_subhost/${graphNames.recommendGraphName}",
                    hardTimeout = "340ms",
                ),
                ServicehostGraphNodeConfig(
                    nodeName = "MEDIATOR_RESPONSE",
                    backendName = mediatorBackendName,
                    nodeDependencies = listOf(
                        "!RECOMMEND@!public_recommendations_response",
                        "!MEDIATOR_REQUEST@!recommends_log_request_context,!proto_recommend_context",
                        "MASTER_TOGGLES",
                    ),
                    handler = "/apphost/mediator/on-response",
                    hardTimeout = "50ms",
                ),
            ),
            graphTransparentNodes = listOf(
                ServicehostGraphTransparentNodeConfig(
                    nodeName = "COMMON_REQUEST_CONTEXT",
                    nodeDependencies = listOf(
                        "MASTER_TOGGLES",
                        "!MEDIATOR_REQUEST@!recommender_internal_request,!proto_recommend_context",
                    ),
                ),
            ),
        )
    }

    private fun buildRecommendGraphConfig(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        gatewayServiceRuntime: ServiceRuntime,
        metaServiceRuntime: ServiceRuntime,
    ): ServicehostGraphConfig {
        val recommenderName = taskContext.recommenderName
        val gatewayBackendName = convertToMeshBackendName(gatewayServiceRuntime.cloudServiceName)
        val metaBackendName = convertToMeshBackendName(metaServiceRuntime.cloudServiceName)

        return ServicehostGraphConfig(
            graphName = graphNames.recommendGraphName,
            graphInputDeps = listOf("STORAGES", "COMMON_REQUEST_CONTEXT"),
            graphOutputDeps = mapOf(
                "RESPONSE" to listOf("!META_RECOMMENDER"),
            ),
            responsibles = listOf(taskContext.serviceOwner),
            graphNodes = listOf(
                ServicehostGraphNodeConfig(
                    nodeName = "RECOMMENDER_GATEWAY",
                    backendName = gatewayBackendName,
                    teamcityProject = gatewayServiceRuntime.teamcityProject,
                    nodeDependencies = listOf("COMMON_REQUEST_CONTEXT"),
                    handler = "/apphost/platform/gateway",
                    hardTimeout = "70ms",
                ),
                ServicehostGraphNodeConfig(
                    nodeName = "BASE_RECOMMENDER",
                    backendName = "SELF",
                    nodeDependencies = listOf(
                        "COMMON_REQUEST_CONTEXT",
                        "RECOMMENDER_GATEWAY@platform_proto_user_info",
                        "STORAGES@bifrost_user_data_response",
                    ),
                    handler = "/_subhost/${graphNames.recommendBaseGraphName}",
                    hardTimeout = "150ms",
                ),
                ServicehostGraphNodeConfig(
                    nodeName = "META_RECOMMENDER",
                    backendName = metaBackendName,
                    teamcityProject = metaServiceRuntime.teamcityProject,
                    nodeDependencies = listOf(
                        "COMMON_REQUEST_CONTEXT",
                        "BASE_RECOMMENDER@platform_base_recommender_response",
                        "RECOMMENDER_GATEWAY@platform_proto_user_info",
                    ),
                    handler = "/apphost/meta/$recommenderName",
                    hardTimeout = "100ms",
                ),
            ),
        )
    }

    private fun buildRecommendBaseGraphConfig(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        baseServiceRuntime: ServiceRuntime,
    ): ServicehostGraphConfig {
        val baseBackendName = convertToMeshBackendName(baseServiceRuntime.cloudServiceName)

        return ServicehostGraphConfig(
            graphName = graphNames.recommendBaseGraphName,
            allowEmptyResponse = "false",
            edgeExpressions = mapOf(
                "SHARD_{{SHARD}}->BASE_RECOMMENDER_RESPONSE_AGGREGATE" to
                        "!RECOMMENDER_GATEWAY[disable_base_shard_{{SHARD}}]",
            ),
            graphInputDeps = listOf("COMMON_REQUEST_CONTEXT", "RECOMMENDER_GATEWAY", "STORAGES"),
            graphOutputDeps = mapOf(
                "BASE_RECOMMENDER_RESPONSE" to listOf(
                    "BASE_RECOMMENDER_RESPONSE_AGGREGATE@platform_base_recommender_response",
                ),
            ),
            responsibles = listOf(taskContext.serviceOwner),
            graphShardedNodes = listOf(
                ServicehostGraphShardedNodeConfig(
                    nodeName = "SHARD",
                    backendName = baseBackendName,
                    teamcityProject = baseServiceRuntime.teamcityProject,
                    shardCount = requireShardsCount(baseServiceRuntime),
                    nodeDependencies = listOf(
                        "!COMMON_REQUEST_CONTEXT",
                        "RECOMMENDER_GATEWAY@platform_proto_user_info,consumption_hashes_shard_{{SHARD}}->consumption_hashes",
                        "STORAGES@bifrost_user_data_response",
                    ),
                    handler = "/apphost/recommend",
                    hardTimeout = "130ms",
                ),
            ),
            graphTransparentNodes = listOf(
                ServicehostGraphTransparentNodeConfig(
                    nodeName = "BASE_RECOMMENDER_RESPONSE_AGGREGATE",
                    nodeDependencies = listOf("SHARD_{{SHARD}}@platform_base_recommender_response"),
                ),
            ),
        )
    }

    private fun buildOfflineGraphConfig(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        factorProxyServiceRuntime: ServiceRuntime,
    ): ServicehostGraphConfig {
        val factorProxyBackendName = convertToMeshBackendName(factorProxyServiceRuntime.cloudServiceName)

        return ServicehostGraphConfig(
            graphName = graphNames.offlineGraphName,
            graphInputDeps = listOf("I2I_REQUEST"),
            graphOutputDeps = mapOf(
                "RESPONSE" to listOf("!RECOMMENDER@!i2i_recommender_response"),
            ),
            responsibles = listOf(taskContext.serviceOwner),
            graphNodes = listOf(
                ServicehostGraphNodeConfig(
                    nodeName = "FACTOR_PROXY",
                    backendName = factorProxyBackendName,
                    teamcityProject = factorProxyServiceRuntime.teamcityProject,
                    nodeDependencies = listOf("!I2I_REQUEST@!factor_proxy_request,!proto_recommend_context"),
                    handler = "/apphost/get-factors",
                    hardTimeout = "10000ms",
                    softTimeout = "5000ms",
                ),
                ServicehostGraphNodeConfig(
                    nodeName = "RECOMMENDER",
                    backendName = "SELF",
                    nodeDependencies = listOf(
                        "!I2I_REQUEST@!i2i_request,!proto_recommend_context",
                        "!FACTOR_PROXY->ANCHOR_DATA@!factor_proxy_response",
                    ),
                    handler = "/_subhost/${graphNames.offlineRecommendGraphName}",
                    hardTimeout = "30000ms",
                    softTimeout = "15000ms",
                ),
            ),
        )
    }

    private fun buildOfflineRecommendGraphConfig(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        metaI2IServiceRuntime: ServiceRuntime,
    ): ServicehostGraphConfig {
        val metaI2IBackendName = convertToMeshBackendName(metaI2IServiceRuntime.cloudServiceName)

        return ServicehostGraphConfig(
            graphName = graphNames.offlineRecommendGraphName,
            graphInputDeps = listOf("I2I_REQUEST", "ANCHOR_DATA"),
            graphOutputDeps = mapOf(
                "RESPONSE" to listOf("!META_I2I@!i2i_recommender_meta_response->i2i_recommender_response"),
            ),
            responsibles = listOf(taskContext.serviceOwner),
            graphNodes = listOf(
                ServicehostGraphNodeConfig(
                    nodeName = "BASE_I2I",
                    backendName = "SELF",
                    nodeDependencies = listOf(
                        "!I2I_REQUEST@!i2i_request,!proto_recommend_context",
                        "!ANCHOR_DATA",
                    ),
                    handler = "/_subhost/${graphNames.offlineBaseGraphName}",
                    hardTimeout = "20000ms",
                    softTimeout = "10000ms",
                ),
                ServicehostGraphNodeConfig(
                    nodeName = "META_I2I",
                    backendName = metaI2IBackendName,
                    teamcityProject = metaI2IServiceRuntime.teamcityProject,
                    nodeDependencies = listOf(
                        "!I2I_REQUEST@!i2i_request,!proto_recommend_context",
                        "!ANCHOR_DATA",
                        "!BASE_I2I@!i2i_recommender_base_response",
                    ),
                    handler = "/apphost/i2i-meta",
                    hardTimeout = "10000ms",
                    softTimeout = "5000ms",
                ),
            ),
        )
    }

    private fun buildOfflineBaseGraphConfig(
        taskContext: ServicehostTaskContext,
        graphNames: ServicehostGraphNames,
        baseI2IServiceRuntime: ServiceRuntime,
    ): ServicehostGraphConfig {
        val baseI2IBackendName = convertToMeshBackendName(baseI2IServiceRuntime.cloudServiceName)

        return ServicehostGraphConfig(
            graphName = graphNames.offlineBaseGraphName,
            allowEmptyResponse = "false",
            graphInputDeps = listOf("I2I_REQUEST", "ANCHOR_DATA"),
            graphOutputDeps = mapOf(
                "BASE_I2I_RESPONSE" to listOf(
                    "BASE_I2I_RESPONSE_AGGREGATE@i2i_recommender_base_response",
                ),
            ),
            responsibles = listOf(taskContext.serviceOwner),
            graphShardedNodes = listOf(
                ServicehostGraphShardedNodeConfig(
                    nodeName = "SHARD",
                    backendName = baseI2IBackendName,
                    teamcityProject = baseI2IServiceRuntime.teamcityProject,
                    shardCount = requireShardsCount(baseI2IServiceRuntime),
                    nodeDependencies = listOf(
                        "!I2I_REQUEST@!i2i_request,!proto_recommend_context",
                        "!ANCHOR_DATA",
                    ),
                    handler = "/apphost/i2i-base",
                    hardTimeout = "20000ms",
                    softTimeout = "10000ms",
                ),
            ),
            graphTransparentNodes = listOf(
                ServicehostGraphTransparentNodeConfig(
                    nodeName = "BASE_I2I_RESPONSE_AGGREGATE",
                    nodeDependencies = listOf("SHARD_{{SHARD}}@i2i_recommender_base_response"),
                ),
            ),
        )
    }

    private fun buildStoragesGraphConfig(
        serviceOwner: String,
        graphNames: ServicehostGraphNames,
        ytProxyServiceRuntime: ServiceRuntime,
        ytProxyBackendName: String,
        ytProxyTenantName: String,
    ): ServicehostGraphConfig {
        return ServicehostGraphConfig(
            graphName = graphNames.storagesGraphName,
            graphInputDeps = listOf("MEDIATOR"),
            graphOutputDeps = mapOf(
                "RESPONSE" to listOf("YT_PROXY_USER_DATA@bifrost_user_data_response"),
            ),
            responsibles = listOf(serviceOwner),
            graphNodes = listOf(
                ServicehostGraphNodeConfig(
                    nodeName = "YT_PROXY_USER_DATA",
                    backendName = convertToMeshBackendName(ytProxyBackendName),
                    teamcityProject = ytProxyServiceRuntime.teamcityProject,
                    nodeDependencies = listOf("TENANT", "MEDIATOR@yt_proxy_user_data_request"),
                    maxReaskBudget = "10.0",
                    requestsPerReask = "100",
                    handler = "/apphost/bifrost_user_data",
                    hardTimeout = "90ms",
                ),
            ),
            graphEmbedNodes = listOf(ytProxyTenantNode(ytProxyTenantName)),
        )
    }

    private fun resolveYtProxyBackendName(ytProxyServiceRuntime: ServiceRuntime): String {
        return ytProxyServiceRuntime.cloudServiceName
    }

    private fun convertToMeshBackendName(rawBackendName: String): String {
        return rawBackendName.replace("-", "_").uppercase()
    }

    private fun requireServiceRuntime(
        serviceRuntimes: List<ServiceRuntime>,
        serviceType: ServiceType,
    ): ServiceRuntime {
        return serviceRuntimes.firstOrNull { serviceRuntime -> serviceRuntime.type == serviceType }
            ?: error("$serviceType service is required for servicehost generation")
    }

    private fun masterTogglesNode(): ServicehostGraphNodeConfig {
        return ServicehostGraphNodeConfig(
            nodeName = "MASTER_TOGGLES",
            backendName = "MASTER_TOGGLES_EXT",
            nodeDependencies = listOf("PROTO_HTTP_REQUEST", "TENANT"),
            handler = "/apphost/master-toggles",
            responsibles = listOf("r.kalganov", "r.aliyarov"),
            hardTimeout = "70ms",
            retryOn = "gateway-error,connect-failure,envoy-ratelimited,reset",
        )
    }

    private fun togglesTenantNode(tenantName: String): ServicehostGraphEmbedNodeConfig {
        return ServicehostGraphEmbedNodeConfig(
            nodeName = "\"TENANT\"",
            params = listOf(
                mapOf(
                    "tenant" to "\"$tenantName\"",
                    "type" to "\"toggles_tenant\"",
                ),
            ),
        )
    }

    private fun ytProxyTenantNode(tenantName: String): ServicehostGraphEmbedNodeConfig {
        return ServicehostGraphEmbedNodeConfig(
            nodeName = "\"TENANT\"",
            params = listOf(
                mapOf(
                    "yt_proxy_tenant" to "\"$tenantName\"",
                    "type" to "\"yt_proxy_tenant\"",
                ),
            ),
        )
    }

    private data class ServicehostGenerationContext(
        val recommenderName: String,
        val servicehostRootDirectory: Path,
        val fullClusterName: String,
        val graphNames: ServicehostGraphNames,
    )

    private data class BackendGenerationPlan(
        val regularBackends: List<ServiceRuntime>,
        val shardedBackends: List<ShardedBackendGeneration>,
    )

    private data class ShardedBackendGeneration(
        val serviceRuntime: ServiceRuntime,
        val shardCount: Int,
    )

    private data class GraphGenerationPlan(
        val graphConfigs: List<ServicehostGraphConfig>,
        val shootingTestGraphNames: List<String>,
        val entryGraphName: String?,
    )

    private data class OnlineServiceRuntimes(
        val baseServiceRuntime: ServiceRuntime,
        val metaServiceRuntime: ServiceRuntime,
        val gatewayServiceRuntime: ServiceRuntime,
        val mediatorServiceRuntime: ServiceRuntime,
        val ytProxyServiceRuntime: ServiceRuntime,
    )

    private data class OfflineServiceRuntimes(
        val factorProxyServiceRuntime: ServiceRuntime,
        val metaI2IServiceRuntime: ServiceRuntime,
        val baseI2IServiceRuntime: ServiceRuntime,
    )

    private companion object {
        private val ONLINE_SERVICE_TYPES = setOf(
            ServiceType.BASE,
            ServiceType.META,
            ServiceType.GATEWAY,
            ServiceType.PLATFORM_GATEWAY,
            ServiceType.MEDIATOR,
            ServiceType.YT_PROXY,
        )

        private val OFFLINE_SERVICE_TYPES = setOf(
            ServiceType.FACTOR_PROXY,
            ServiceType.META_I2I,
            ServiceType.BASE_I2I,
            ServiceType.SCHEDULER_I2I,
        )

        private val ALL_SERVICEHOST_ENVIRONMENTS = ServiceEnvironment.entries.toSet()
        private val PRODUCTION_ONLY_SERVICEHOST_ENVIRONMENTS = setOf(ServiceEnvironment.PRODUCTION)
        private const val OFFLINE_PAYLOAD_ARTIFACT_SUFFIX = "-offline"
    }
}
