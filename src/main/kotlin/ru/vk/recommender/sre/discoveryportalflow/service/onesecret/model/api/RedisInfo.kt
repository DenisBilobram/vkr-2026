package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.SECRET_PLACEHOLDER

@JsonIgnoreProperties(ignoreUnknown = true)
data class RedisInfo(
    val hosts: String = SECRET_PLACEHOLDER,
    val password: String = SECRET_PLACEHOLDER,
    val user: String = SECRET_PLACEHOLDER,
)
