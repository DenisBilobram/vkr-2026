package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec

interface OneSecretInfoCodec<InfoType> {
    val readKeys: Set<String>

    fun toSecretData(info: InfoType): MutableMap<String, String>

    fun fromSecretData(secretData: Map<String, String>): InfoType
}
