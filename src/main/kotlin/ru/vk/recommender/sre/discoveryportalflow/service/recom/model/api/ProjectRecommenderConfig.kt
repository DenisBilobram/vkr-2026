package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectRecommenderConfig(
    val projectName: String,
    val serviceOwner: String,
    val productId: Int? = null,
    val ytOnlineRobotSecretId: String? = null,
    val ytOfflineRobotSecretId: String? = null,
)
