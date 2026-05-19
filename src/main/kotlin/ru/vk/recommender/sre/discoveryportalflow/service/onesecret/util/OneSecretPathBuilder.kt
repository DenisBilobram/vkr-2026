package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.util

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironmentRuntime

object OneSecretPathBuilder {

    fun build(
        environmentRuntime: ServiceEnvironmentRuntime,
        secretId: String,
    ): String {
        val segmentedSecretId = secretId.trim().chunked(3).joinToString(separator = "/")
        val serviceName = environmentRuntime.cloudServiceName
        val rootQueueName = environmentRuntime.rootQueueName
        return "onesecret-kv/s/$serviceName.$rootQueueName/$segmentedSecretId"
    }
}