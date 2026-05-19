package ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime

enum class ServiceEnvironment(
    val id: String,
    val serviceNamePrefix: String,
    val secretsFileName: String?,
) {
    PRODUCTION(
        id = "production",
        serviceNamePrefix = "",
        secretsFileName = "secrets.yml",
    ),
    CANARY(
        id = "canary",
        serviceNamePrefix = "canary-",
        secretsFileName = "secrets.canary.yml",
    ),
    TESTING(
        id = "testing",
        serviceNamePrefix = "testing-",
        secretsFileName = "secrets.prestable.yml",
    );

    fun applyPrefix(cloudServiceName: String): String {
        return "$serviceNamePrefix$cloudServiceName"
    }
}
