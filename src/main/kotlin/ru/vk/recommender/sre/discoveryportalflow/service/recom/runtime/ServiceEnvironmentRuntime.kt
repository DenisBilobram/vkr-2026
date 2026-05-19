package ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime

data class ServiceEnvironmentRuntime(
    val environment: ServiceEnvironment,
    val cloudServiceName: String,
    val applicationSecretDirectoryName: String,
    val rootQueueName: String,
) {
    val cloudQueueId: String
        get() = "$cloudServiceName.$rootQueueName"

    val pmsHost: String
        get() = "$cloudServiceName.clouds"

    val applicationSecretPath: String
        get() = "/etc/$applicationSecretDirectoryName/application-secret.properties"
}
