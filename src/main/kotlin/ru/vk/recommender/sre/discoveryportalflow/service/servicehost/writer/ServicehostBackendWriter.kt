package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.writeText
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.context.ServicehostTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostBackendConfig
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostRequestPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.renderer.ServicehostYamlRawDumper
import java.nio.file.Path

class ServicehostBackendWriter {

    fun writeBackendMeshConfigs(
        taskContext: ServicehostTaskContext,
        serviceRuntime: ServiceRuntime,
        servicehostRootDirectory: Path,
        queueName: String,
        backendConfigs: List<ServicehostBackendConfig>,
        servicehostRequestPayload: ServicehostRequestPayload,
        serviceNamesByEnvironment: MutableMap<String, MutableList<String>>,
        servicehostEnvironments: Set<ServiceEnvironment>,
    ) {
        val clusterConfigDirectory = servicehostRootDirectory
            .resolve("projects/public/mesh-clusters/public-prod/mesh-service-configs/public-${taskContext.recommenderName}-prod")

        val environmentSetups = listOf(
            ServiceEnvironment.PRODUCTION to taskContext.dcSettings.productionDcs,
            ServiceEnvironment.CANARY to taskContext.dcSettings.canaryDcs,
            ServiceEnvironment.TESTING to taskContext.dcSettings.testingDcs,
        )

        environmentSetups
            .filter { (environment, _) ->
                environment in servicehostEnvironments &&
                        (environment == ServiceEnvironment.PRODUCTION || serviceRuntime.supports(environment))
            }
            .forEach { (environment, datacenterCodes) ->
                val serviceQueueName = environment.applyPrefix(queueName)
                val documentBlocks = mutableListOf<String>()

                backendConfigs.forEach { backendConfig ->
                    val normalizedBackendName =
                        ServiceEnvironment.entries.fold(backendConfig.backendName) { backendName, knownEnvironment ->
                            backendName.removePrefix(knownEnvironment.serviceNamePrefix)
                        }
                    val prefixedBackendName = environment.applyPrefix(normalizedBackendName)
                    serviceNamesByEnvironment.getValue(environment.id).add(prefixedBackendName)

                    documentBlocks += buildBackendYamlDocuments(
                        backendConfig = backendConfig.copy(backendName = prefixedBackendName),
                        onecloudQueueName = serviceQueueName,
                        datacenterCodes = datacenterCodes,
                    )
                }

                val backendMeshYamlContent = documentBlocks.joinToString(separator = "\n") + "\n"

                writeText(
                    clusterConfigDirectory.resolve("$serviceQueueName-mesh.yaml"),
                    backendMeshYamlContent
                )
                servicehostRequestPayload.addBackend(
                    serviceQueueName,
                    backendMeshYamlContent,
                    environment
                )
            }
    }

    private fun buildBackendYamlDocuments(
        backendConfig: ServicehostBackendConfig,
        onecloudQueueName: String,
        datacenterCodes: List<String>,
    ): String {
        val datacentersListValue = "[${datacenterCodes.joinToString(separator = ", ")}]"

        val documents = listOf(
            linkedMapOf(
                "apiVersion" to "v1",
                "kind" to "MeshServiceDefaults",
                "metadata" to linkedMapOf(
                    "mesh_service_name" to backendConfig.backendName,
                    "cluster" to "public-prod",
                    "datacenters" to datacentersListValue,
                ),
                "spec" to linkedMapOf(
                    "protocol" to "http",
                    "vk_ext" to linkedMapOf(
                        "local_protocol" to "tcp",
                        "disable_mtls" to "true",
                        "disable_rbac" to "true",
                    ),
                ),
            ),
            linkedMapOf(
                "apiVersion" to "v1",
                "kind" to "MeshServiceConfig",
                "metadata" to linkedMapOf(
                    "name" to backendConfig.backendName,
                    "cluster" to "public-prod",
                    "datacenters" to datacentersListValue,
                    "cloud_namespace" to "public",
                    "cloud_service_name" to "${backendConfig.onecloudShardName}.$onecloudQueueName",
                ),
                "spec" to linkedMapOf(
                    "instance_templates" to listOf(
                        linkedMapOf(
                            "service" to backendConfig.backendName,
                            "version" to "1",
                            "expose" to linkedMapOf(
                                "address_type" to "lan6",
                                "port" to "8085",
                                "local_service_address" to "127.0.0.1",
                                "local_service_port" to backendConfig.port.toString(),
                            ),
                            "health_checks" to listOf(
                                linkedMapOf(
                                    "name" to "main",
                                    "interval" to "1s",
                                    "timeout" to "1s",
                                    "http" to "http://localhost:82/ping",
                                )
                            ),
                        )
                    )
                ),
            ),
            linkedMapOf(
                "apiVersion" to "v1",
                "kind" to "MeshServiceResolver",
                "metadata" to linkedMapOf(
                    "mesh_service_name" to backendConfig.backendName,
                    "cluster" to "public-prod",
                    "datacenters" to datacentersListValue,
                ),
                "spec" to linkedMapOf(
                    "connect_timeout" to "50ms",
                    "request_timeout" to "1s",
                    "failover" to linkedMapOf(
                        "'*'" to linkedMapOf(
                            "datacenters" to datacentersListValue,
                        )
                    ),
                    "vk_ext" to linkedMapOf(
                        "failover_mode" to "failover-dc-two-priorities",
                        "failover_overprovisioning_factor" to "140",
                    ),
                ),
            ),
            linkedMapOf(
                "apiVersion" to "v1",
                "kind" to "MeshServiceIntentions",
                "metadata" to linkedMapOf(
                    "mesh_service_name" to backendConfig.backendName,
                    "cluster" to "public-prod",
                    "datacenters" to datacentersListValue,
                ),
                "spec" to linkedMapOf(
                    "sources" to listOf(
                        linkedMapOf(
                            "name" to "'*'",
                            "allow" to "true",
                        )
                    )
                ),
            ),
            linkedMapOf(
                "apiVersion" to "v1",
                "kind" to "MeshServiceRouter",
                "metadata" to linkedMapOf(
                    "mesh_service_name" to backendConfig.backendName,
                    "cluster" to "public-prod",
                    "datacenters" to datacentersListValue,
                ),
                "spec" to linkedMapOf(
                    "routes" to listOf(
                        linkedMapOf(
                            "match" to linkedMapOf(
                                "http" to linkedMapOf(
                                    "path_prefix" to backendConfig.rootPath,
                                )
                            ),
                            "destination" to linkedMapOf(
                                "service" to backendConfig.backendName,
                                "request_timeout" to "1s",
                                "idle_timeout" to "60s",
                                "retry_on" to "[connect-failure, gateway-error]",
                                "num_retries" to "2",
                            ),
                        )
                    ),
                ),
            ),
        )

        return ServicehostYamlRawDumper.dumpDocuments(documents)
    }
}
