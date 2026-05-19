package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.exception

open class RetryableException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
