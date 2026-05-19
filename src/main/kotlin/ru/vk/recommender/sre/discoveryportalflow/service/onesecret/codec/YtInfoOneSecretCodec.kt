package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.YtInfo

class YtInfoOneSecretCodec : OneSecretInfoCodec<YtInfo> {
    private val tokenField = OneSecretField("yt.tokens", YtInfo::token)
    private val userFields = listOf(
        OneSecretField("yt.users", YtInfo::user),
        OneSecretField("yt.user", YtInfo::user),
        OneSecretField("yt.default_user", YtInfo::user),
    )
    private val secretFields = listOf(tokenField) + userFields

    override val readKeys: Set<String> = buildOneSecretKeySet(secretFields)

    override fun toSecretData(info: YtInfo): MutableMap<String, String> {
        return buildOneSecretMapping(info, secretFields)
    }

    override fun fromSecretData(secretData: Map<String, String>): YtInfo {
        val userKeys = userFields.map { field -> field.key }
        val user = userFields.firstNotNullOfOrNull { field -> field.optionalValue(secretData) }
            ?: error("Missing OneSecret YT user key. Expected one of: $userKeys")
        return YtInfo(
            user = user,
            token = tokenField.requiredValue(secretData),
        )
    }
}
