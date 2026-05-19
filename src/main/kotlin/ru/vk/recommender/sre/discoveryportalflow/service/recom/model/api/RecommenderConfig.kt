package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RecommenderConfig(
    val recommenderName: String,
    val serviceOwner: String,
    val productId: Int? = null,
    @JsonAlias("parentProductId")
    val projectProductId: Int? = null,
    @JsonAlias("parentProductName")
    val projectName: String? = null,
    val createMinOneCloudConfiguration: Boolean = false,
    val additionalResponsibles: List<String> = emptyList(),
    val additionalFollowers: List<String> = emptyList(),
)
