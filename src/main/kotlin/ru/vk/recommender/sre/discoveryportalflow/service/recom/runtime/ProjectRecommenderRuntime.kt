package ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectRecommenderRuntime(
    val projectName: String,
    val productId: Int?,
    val serviceOwner: String,
    val productionRootQueueName: String,
    val testingRootQueueName: String,
    val services: List<ServiceRuntime> = emptyList(),
)
