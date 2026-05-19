package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.AppTracerInfo

class AppTracerInfoOneSecretCodec : OneSecretInfoCodec<AppTracerInfo> {
    private val tokenField = OneSecretField("appToken", AppTracerInfo::token)
    private val secretFields = listOf(tokenField)

    override val readKeys: Set<String> = buildOneSecretKeySet(secretFields)

    override fun toSecretData(info: AppTracerInfo): MutableMap<String, String> {
        return buildOneSecretMapping(info, secretFields)
    }

    override fun fromSecretData(secretData: Map<String, String>): AppTracerInfo {
        return AppTracerInfo(
            token = tokenField.requiredValue(secretData),
        )
    }
}
