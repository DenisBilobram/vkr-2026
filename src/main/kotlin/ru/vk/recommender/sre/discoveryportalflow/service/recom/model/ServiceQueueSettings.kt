package ru.vk.recommender.sre.discoveryportalflow.service.recom.model

data class ServiceQueueSettings(
    val rootQueueNames: ServiceQueueNames,
    val pmsRootQueueName: String = rootQueueNames.productionRootQueueName,
    val onecloudSubqueues: List<String> = listOf("java"),
)
