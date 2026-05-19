package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.AppTracerInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.YtInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.client.OneSecretClient
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.OneSecretQueueTarget
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.AppTracerInfo
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.YtInfo
import java.util.LinkedHashMap

class OneSecretService(
    private val oneSecretClient: OneSecretClient,
    private val ytInfoOneSecretCodec: YtInfoOneSecretCodec,
    private val appTracerInfoOneSecretCodec: AppTracerInfoOneSecretCodec,
) {

    fun configureProjectSecret(
        projectName: String,
        projectProductId: Int,
        recommenderName: String,
        apptracerToken: String,
        ytOffline: YtInfo,
        queueTargets: List<OneSecretQueueTarget>,
    ): String {
        return configureServiceSecret(
            alias = buildDiscoverySecretAlias(projectName),
            description = "Project secret for $projectName",
            data = ytInfoOneSecretCodec.toSecretData(ytOffline).apply {
                putAll(appTracerInfoOneSecretCodec.toSecretData(AppTracerInfo(apptracerToken)))
            },
            queueTargets = queueTargets,
            ownerAbcGroupIds = listOf(projectProductId),
            tags = listOf(recommenderName, projectName),
        )
    }

    fun configureVerticalSecrets(
        recommenderName: String,
        projectName: String?,
        productId: Int,
        projectProductId: Int?,
        apptracerToken: String,
        queueTargets: List<OneSecretQueueTarget>,
    ): String {
        return configureServiceSecret(
            alias = buildDiscoverySecretAlias(recommenderName, projectName),
            description = "Shared secret for recommender $recommenderName",
            data = appTracerInfoOneSecretCodec.toSecretData(AppTracerInfo(apptracerToken)),
            queueTargets = queueTargets,
            ownerAbcGroupIds = buildList {
                add(productId)
                projectProductId?.let(::add)
            },
            tags = buildList {
                add(recommenderName)
                projectName?.takeIf { it.isNotBlank() }?.let(::add)
            },
        )
    }

    fun configureServiceSecret(
        alias: String,
        description: String,
        data: Map<String, String>,
        queueTargets: List<OneSecretQueueTarget>,
        ownerAbcGroupIds: List<Int>,
        tags: List<String>,
    ): String {
        val secretId = oneSecretClient.createSecret(
            alias = alias,
            description = description,
            data = LinkedHashMap(data),
            tags = tags,
        )

        shareSecretWithQueues(secretId, queueTargets)
        ownerAbcGroupIds.distinct().forEach { ownerAbcGroupId ->
            grantOwnerAccess(secretId, ownerAbcGroupId)
        }
        return secretId
    }

    fun updateSecretData(
        secretId: String,
        secretData: Map<String, String>,
        comment: String = "Updated by discovery-portal-flow",
    ) {
        require(secretData.isNotEmpty()) {
            "secretData must not be empty for secretId=$secretId"
        }

        val mergedSecretData = LinkedHashMap(oneSecretClient.getLatestSecretData(secretId))
        secretData.forEach { (key, value) ->
            mergedSecretData[key] = value
        }
        oneSecretClient.putSecretVersion(
            secretId = secretId,
            data = mergedSecretData,
            comment = comment,
        )
    }

    fun getSecretData(secretId: String): Map<String, String> {
        return LinkedHashMap(oneSecretClient.getLatestSecretData(secretId))
    }

    fun getSecretData(
        secretId: String,
        requestedKeys: Collection<String>,
    ): Map<String, String> {
        require(requestedKeys.isNotEmpty()) {
            "requestedKeys must not be empty for secretId=$secretId"
        }

        val secretData = oneSecretClient.getLatestSecretData(secretId)
        return requestedKeys
            .distinct()
            .mapNotNull { key ->
                secretData[key]?.let { value -> key to value }
            }
            .toMap(LinkedHashMap())
    }

    private fun shareSecretWithQueues(
        secretId: String,
        queueTargets: List<OneSecretQueueTarget>,
    ) {
        queueTargets.forEach { queueTarget ->
            oneSecretClient.shareWithOnecloudQueue(
                secretId = secretId,
                cloudNamespace = queueTarget.cloudNamespace,
                cloudQueueId = queueTarget.cloudQueueId,
            )
        }
    }

    private fun grantOwnerAccess(
        secretId: String,
        abcGroupId: Int,
    ) {
        oneSecretClient.grantAbcGroupAccess(
            secretId = secretId,
            abcGroupId = abcGroupId.toString(),
            role = OWNER_ROLE,
        )
    }

    internal fun buildDiscoverySecretAlias(
        vararg aliasParts: String?,
    ): String {
        return aliasParts
            .filterNotNull()
            .filter { it.isNotBlank() }
            .joinToString(separator = ".", postfix = ".discovery")
    }

    private companion object {
        private const val OWNER_ROLE = "owner"
    }
}
