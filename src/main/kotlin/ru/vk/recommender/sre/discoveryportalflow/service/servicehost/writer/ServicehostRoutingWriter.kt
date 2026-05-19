package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.template.TemplateFileRenderer
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.writeText
import java.nio.file.Path

class ServicehostRoutingWriter {

    fun writeRoutingClusterFiles(
        servicehostRootDirectory: Path,
        fullClusterName: String,
        entryGraphName: String,
        serviceNamesByEnvironment: Map<String, List<String>>,
        servicehostEnvironments: Set<ServiceEnvironment>,
    ) {
        val routingConfigContent = TemplateFileRenderer.render(
            "templates/genericrecom/servicehost/routing_config.json",
            mapOf("EXPORT_GRAPH_NAME" to entryGraphName),
        )
        val servicehostConfigContent = TemplateFileRenderer.render(
            "templates/genericrecom/servicehost/servicehost.conf",
            mapOf("EXPORT_GRAPH_NAME" to entryGraphName),
        ) + "\n"

        val clusterFolderMappings = buildList {
            if (ServiceEnvironment.TESTING in servicehostEnvironments) {
                add(Triple("testing-servicehost", serviceNamesByEnvironment.getValue("testing"), true))
                add(Triple("testing-servicehost-new", serviceNamesByEnvironment.getValue("testing"), false))
            }
            if (ServiceEnvironment.CANARY in servicehostEnvironments) {
                add(Triple("canary-servicehost", serviceNamesByEnvironment.getValue("canary"), true))
                add(Triple("canary-servicehost-new", serviceNamesByEnvironment.getValue("canary"), false))
            }
            if (ServiceEnvironment.PRODUCTION in servicehostEnvironments) {
                add(Triple("servicehost", serviceNamesByEnvironment.getValue("production"), true))
                add(Triple("servicehost-new", serviceNamesByEnvironment.getValue("production"), false))
            }
        }

        clusterFolderMappings.forEach { (clusterFolderName, meshServiceNames, shouldUseRoutingConfig) ->
            val clusterFolder = servicehostRootDirectory
            .resolve("projects/public/servicehost-clusters/$fullClusterName/$clusterFolderName")

            val meshServicesContent = meshServiceNames.joinToString(separator = "\n") {
                meshServiceName -> "    - service: \"$meshServiceName\""
            } + "\n"

            writeText(clusterFolder.resolve("mesh.yaml"), meshServicesContent)

            if (shouldUseRoutingConfig) {
                writeText(clusterFolder.resolve("files/routing_config.json"), routingConfigContent)
            } else {
                writeText(clusterFolder.resolve("files/etc/servicehost.conf"), servicehostConfigContent)
            }
        }
    }
}
