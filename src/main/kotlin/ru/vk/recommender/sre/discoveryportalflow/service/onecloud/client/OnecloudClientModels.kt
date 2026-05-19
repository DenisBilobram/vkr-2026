package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode

data class OnecloudDatacenterResponse<T>(
    val dc: DatacenterCode,
    val response: T,
)

data class OnecloudSubmissionResult<T>(
    val submitted: List<OnecloudDatacenterResponse<T>>,
    val skippedExistingDcs: List<DatacenterCode>,
)

enum class OnecloudServiceStatus {
    RUNNING,
    FAILED,
    STARTING,
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudServiceManifest(
    val type: String? = null,
    val namespace: String? = null,
    val name: String? = null,
    val queue: String? = null,
    val env: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudServiceInfo(
    val namespace: String? = null,
    val hierarchy: String? = null,
    val name: String? = null,
    val state: String? = null,
    val promise: String? = null,
    val containers: List<OnecloudSubServiceInfo> = emptyList(),
    val restartOn: String? = null,
    val alerts: Map<String, String> = emptyMap(),
    val availability: String? = null,
    @JsonProperty("availability_rank")
    val availabilityRank: Float? = null,
    val outcome: String? = null,
    val outcomeText: String? = null,
    val progress: String? = null,
    val submitted: Long? = null,
    val submitter: String? = null,
    val updated: Long? = null,
    val updater: String? = null,
    val lastid: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudSubServiceInfo(
    val instances: List<OnecloudTaskInfo> = emptyList(),
    val image: String? = null,
    val imageTimestamp: Long? = null,
    val imageNotFound: Boolean = false,
    val baseImage: String? = null,
    val baseImageTimestamp: Long? = null,
    val baseImageNotFound: Boolean = false,
    val imageAlert: String? = null,
    val replicas: Int? = null,
    val progress: String? = null,
    val storage: String? = null,
    val storages: List<String> = emptyList(),
    val alerts: Map<String, String> = emptyMap(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudTaskInfo(
    val state: String? = null,
    val outcome: String? = null,
    @JsonProperty("outcome_text")
    val outcomeText: String? = null,
    val progress: String? = null,
    val alerts: Map<String, String> = emptyMap(),
)
