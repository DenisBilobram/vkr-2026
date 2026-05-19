package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.read

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.AppTracerInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretReadTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretService

class ReadAppTracerTokenFromSecretTask(
    oneSecretService: OneSecretService,
    private val appTracerInfoOneSecretCodec: AppTracerInfoOneSecretCodec,
) : AbstractReadOneSecretTask(oneSecretService) {

    override fun resolveRequestedKeys(taskRunContext: OneSecretReadTaskContext): List<String> {
        return appTracerInfoOneSecretCodec.readKeys.toList()
    }

    override fun applyResolvedSecretData(
        taskRunContext: OneSecretReadTaskContext,
        resolvedSecretData: Map<String, String>,
    ) {
        taskRunContext.apptracerToken = appTracerInfoOneSecretCodec.fromSecretData(resolvedSecretData).token
    }
}
