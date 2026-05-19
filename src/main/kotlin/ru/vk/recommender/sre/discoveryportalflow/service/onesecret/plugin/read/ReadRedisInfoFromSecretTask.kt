package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.read

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.RedisInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretReadTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretService

class ReadRedisInfoFromSecretTask(
    oneSecretService: OneSecretService,
    private val redisInfoOneSecretCodec: RedisInfoOneSecretCodec,
) : AbstractReadOneSecretTask(oneSecretService) {

    override fun resolveRequestedKeys(taskRunContext: OneSecretReadTaskContext): List<String> {
        return redisInfoOneSecretCodec.readKeys.toList()
    }

    override fun applyResolvedSecretData(
        taskRunContext: OneSecretReadTaskContext,
        resolvedSecretData: Map<String, String>,
    ) {
        taskRunContext.redis = redisInfoOneSecretCodec.fromSecretData(resolvedSecretData)
    }
}
