package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.client

import com.fasterxml.jackson.databind.ObjectMapper

class OneSecretClient(
    private val objectMapper: ObjectMapper,
    private val oneSecretProperties: OneSecretProperties,
) {

    fun createSecret(
        alias: String,
        description: String,
        data: Map<String, String>,
        tags: List<String> = DEFAULT_TAGS,
    ): String {
        ndaStub("createSecret")
        return "nda-secret-$alias"
    }

    fun getLatestSecretData(secretId: String): Map<String, String> {
        ndaStub("getLatestSecretData")
        return emptyMap()
    }

    fun putSecretVersion(
        secretId: String,
        data: Map<String, String>,
        comment: String,
        idempotencyKey: String = "nda",
    ) {
        ndaStub("putSecretVersion")
    }

    fun shareWithOnecloudQueue(
        secretId: String,
        cloudNamespace: String,
        cloudQueueId: String,
    ) {
        ndaStub("shareWithOnecloudQueue")
    }

    fun grantAbcGroupAccess(
        secretId: String,
        abcGroupId: String,
        role: String,
    ) {
        ndaStub("grantAbcGroupAccess")
    }

    private fun ndaStub(operation: String) {
        // NDA code removed: production implementation calls an internal secret storage API.
        objectMapper.createObjectNode().put("operation", operation).put("baseUrl", oneSecretProperties.baseUrl)
    }

    private companion object {
        private val DEFAULT_TAGS = listOf("discovery-portal-flow")
    }
}
