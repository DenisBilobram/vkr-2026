package ru.vk.recommender.sre.discoveryportalflow.service.recom.model

const val BATCH_PRODUCTION_ROOT_QUEUE_NAME = "app.production.recommender.batch"

data class ServiceQueueNames(
    val productionRootQueueName: String,
    val testingRootQueueName: String,
)
