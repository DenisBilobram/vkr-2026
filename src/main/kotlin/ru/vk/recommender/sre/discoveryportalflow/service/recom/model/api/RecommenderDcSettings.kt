package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RecommenderDcSettings(
    val productionDcs: List<String> = listOf("pc", "uc", "hc"),
    val canaryDcs: List<String> = listOf("pc"),
    val testingDcs: List<String> = listOf("pc"),
    val migrationDcs: List<String> = emptyList(),
)