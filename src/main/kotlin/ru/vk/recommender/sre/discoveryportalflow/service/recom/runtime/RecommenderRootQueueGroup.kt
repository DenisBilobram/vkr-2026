package ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime

data class RecommenderRootQueueGroup(
    val label: String,
    val productionRootQueueName: String,
    val testingRootQueueName: String? = null,
    val productId: Int? = null,
)
