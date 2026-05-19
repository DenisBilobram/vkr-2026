package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.client

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.config.ServicehostAdminProperties
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostClusterInitializationResult
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostConfigApplyResult
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostFile
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostRequestPayload
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostResourceResponse

class ServicehostAdminClient(
    private val objectMapper: ObjectMapper,
    private val servicehostAdminProperties: ServicehostAdminProperties,
) {

    fun initializeCluster(
        fullClusterName: String,
        clusterRequestPayload: Map<String, Any>,
    ): ServicehostClusterInitializationResult {
        ndaStub("initializeCluster")
        return ServicehostClusterInitializationResult(
            fullClusterName = fullClusterName,
            createdNow = false,
            clusterMrUrl = null,
        )
    }

    fun applyClusterConfig(
        fullClusterName: String,
        servicehostRequestPayload: ServicehostRequestPayload,
    ): ServicehostConfigApplyResult {
        ndaStub("applyClusterConfig")
        return ServicehostConfigApplyResult(fullClusterName = fullClusterName, configMrUrl = null)
    }

    fun getBackends(clusterName: String): ServicehostResourceResponse {
        ndaStub("getBackends")
        return ServicehostResourceResponse(emptyList())
    }

    fun getBackend(clusterName: String, name: String): ServicehostFile {
        ndaStub("getBackend")
        return ServicehostFile(name = name, path = name, content = "")
    }

    fun getGraphs(clusterName: String): ServicehostResourceResponse {
        ndaStub("getGraphs")
        return ServicehostResourceResponse(emptyList())
    }

    fun getClusters(): List<String> {
        ndaStub("getClusters")
        return emptyList()
    }

    fun getGraph(clusterName: String, name: String): ServicehostFile {
        ndaStub("getGraph")
        return ServicehostFile(name = name, path = name, content = "")
    }

    fun clusterExist(fullClusterName: String): Boolean {
        ndaStub("clusterExist")
        return false
    }

    private fun ndaStub(operation: String) {
        // NDA code removed: production implementation calls an internal Servicehost admin API.
        objectMapper.createObjectNode()
            .put("operation", operation)
            .put("baseUrl", servicehostAdminProperties.baseUrl)
    }
}
