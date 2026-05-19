package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.writeText
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.context.ServicehostTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostBackendTestsPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostRequestPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.uniqueDatacenterCodes
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.renderer.ServicehostBackendsYamlRenderer
import java.nio.file.Path

class ServicehostApiPayloadWriter(
    private val objectMapper: ObjectMapper,
) {

    fun writeClusterRequestPayload(
        taskContext: ServicehostTaskContext,
        servicehostRootDirectory: Path,
        normalizedClusterName: String,
    ): Map<String, Any> {
        val clusterRequestPayload = mapOf(
            "clusterName" to normalizedClusterName,
            "dcs" to taskContext.dcSettings.uniqueDatacenterCodes().map { datacenterCode -> datacenterCode.uppercase() },
        )

        val apiDirectory = servicehostRootDirectory.resolve("api")
        writeText(
            apiDirectory.resolve("cluster_request.json"),
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(clusterRequestPayload),
        )
        return clusterRequestPayload
    }

    fun writeVerticalPayloads(
        servicehostRootDirectory: Path,
        servicehostRequestPayload: ServicehostRequestPayload,
        backendTestsPayload: ServicehostBackendTestsPayload,
        fullClusterName: String,
        artifactSuffix: String = "",
    ) {
        val apiDirectory = servicehostRootDirectory.resolve("api")
        writeText(
            apiDirectory.resolve("servicehost_request$artifactSuffix.json"),
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(servicehostRequestPayload),
        )

        val backendTestsYaml = ServicehostBackendsYamlRenderer.render(backendTestsPayload)
        writeText(
            servicehostRootDirectory.resolve("test_maps/$fullClusterName$artifactSuffix.yaml"),
            backendTestsYaml,
        )
    }
}
