package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.RedisInfo

class RedisInfoOneSecretCodec : OneSecretInfoCodec<RedisInfo> {
    private val hostsField = OneSecretField("redis.hosts", RedisInfo::hosts)
    private val passwordField = OneSecretField("redis.password", RedisInfo::password)
    private val userField = OneSecretField("redis.user", RedisInfo::user)
    private val secretFields = listOf(hostsField, passwordField, userField)

    override val readKeys: Set<String> = buildOneSecretKeySet(secretFields)

    override fun toSecretData(info: RedisInfo): MutableMap<String, String> {
        return buildOneSecretMapping(info, secretFields)
    }

    override fun fromSecretData(secretData: Map<String, String>): RedisInfo {
        return RedisInfo(
            hosts = hostsField.requiredValue(secretData),
            password = passwordField.requiredValue(secretData),
            user = userField.requiredValue(secretData),
        )
    }
}
