package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OneSecretSettings(
    val apptracerToken: String? = null,
    val ytOffline: YtInfo = YtInfo(),
    val redis: RedisInfo = RedisInfo(),
)