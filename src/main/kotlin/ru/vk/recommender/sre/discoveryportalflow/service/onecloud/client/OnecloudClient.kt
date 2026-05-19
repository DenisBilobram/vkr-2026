package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.config.OnecloudProperties

class OnecloudClient(
    private val objectMapper: ObjectMapper,
    private val onecloudProperties: OnecloudProperties,
) {

    fun submitQueue(
        queueJson: String,
        user: Any,
        dcs: List<DatacenterCode>,
    ): List<OnecloudDatacenterResponse<JsonNode>> =
        dcs.responses("submitQueue") { ndaJson("submitQueue") }

    fun submitQueueIfAbsent(
        queueJson: String,
        user: Any,
        dcs: List<DatacenterCode>,
    ): OnecloudSubmissionResult<JsonNode> =
        OnecloudSubmissionResult(submitted = submitQueue(queueJson, user, dcs), skippedExistingDcs = emptyList())

    fun queueExists(queueName: String, dcs: List<DatacenterCode>): List<OnecloudDatacenterResponse<Boolean>> =
        dcs.responses("queueExists") { false }

    fun submitService(
        serviceJson: String,
        queue: String? = null,
        replicas: String? = null,
        minRunning: String? = null,
        pause: String? = null,
        dcs: List<DatacenterCode>,
    ): List<OnecloudDatacenterResponse<JsonNode>> =
        dcs.responses("submitService") { ndaJson("submitService") }

    fun submitServiceIfAbsent(
        serviceJson: String,
        queue: String? = null,
        replicas: String? = null,
        minRunning: String? = null,
        pause: String? = null,
        dcs: List<DatacenterCode>,
    ): OnecloudSubmissionResult<JsonNode> =
        OnecloudSubmissionResult(
            submitted = submitService(serviceJson, queue, replicas, minRunning, pause, dcs),
            skippedExistingDcs = emptyList(),
        )

    fun serviceExists(serviceName: String, dcs: List<DatacenterCode>): List<OnecloudDatacenterResponse<Boolean>> =
        dcs.responses("serviceExists") { false }

    fun serviceInfoExists(serviceName: String, dcs: List<DatacenterCode>): List<OnecloudDatacenterResponse<Boolean>> =
        dcs.responses("serviceInfoExists") { false }

    fun submitStorage(
        storageJson: String,
        queue: String? = null,
        shards: Int? = null,
        dcs: List<DatacenterCode>,
    ): List<OnecloudDatacenterResponse<JsonNode>> =
        dcs.responses("submitStorage") { ndaJson("submitStorage") }

    fun submitStorageIfAbsent(
        storageJson: String,
        queue: String? = null,
        shards: Int? = null,
        dcs: List<DatacenterCode>,
    ): OnecloudSubmissionResult<JsonNode> =
        OnecloudSubmissionResult(
            submitted = submitStorage(storageJson, queue, shards, dcs),
            skippedExistingDcs = emptyList(),
        )

    fun storageExists(storageName: String, dcs: List<DatacenterCode>): List<OnecloudDatacenterResponse<Boolean>> =
        dcs.responses("storageExists") { false }

    fun getServiceStatus(
        serviceName: String,
        dcs: List<DatacenterCode>,
    ): List<OnecloudDatacenterResponse<OnecloudServiceStatus>> =
        dcs.responses("getServiceStatus") { OnecloudServiceStatus.STARTING }

    fun getServiceInfo(
        serviceName: String,
        dcs: List<DatacenterCode>,
    ): List<OnecloudDatacenterResponse<OnecloudServiceInfo>> =
        dcs.responses("getServiceInfo") { OnecloudServiceInfo(name = serviceName, state = "NDA_REMOVED") }

    fun getRawServiceStatus(
        serviceName: String,
        period: String? = null,
        dcs: List<DatacenterCode>,
    ): List<OnecloudDatacenterResponse<JsonNode>> =
        dcs.responses("getRawServiceStatus") { ndaJson("getRawServiceStatus") }

    fun getServiceManifest(
        serviceName: String,
        dcs: List<DatacenterCode>,
    ): List<OnecloudDatacenterResponse<OnecloudServiceManifest>> =
        dcs.responses("getServiceManifest") { OnecloudServiceManifest(name = serviceName) }

    fun getServiceManifestInDc(serviceName: String, dc: DatacenterCode): OnecloudServiceManifest {
        ndaStub("getServiceManifestInDc")
        return OnecloudServiceManifest(name = serviceName)
    }

    private fun <T> List<DatacenterCode>.responses(operation: String, value: () -> T): List<OnecloudDatacenterResponse<T>> {
        ndaStub(operation)
        return distinct().map { dc -> OnecloudDatacenterResponse(dc = dc, response = value()) }
    }

    private fun ndaJson(operation: String): JsonNode =
        objectMapper.createObjectNode()
            .put("operation", operation)
            .put("status", "NDA code removed")

    private fun ndaStub(operation: String) {
        // NDA code removed: production implementation calls internal OneCloud APIs with TLS credentials.
        onecloudProperties.namespace
    }
}
