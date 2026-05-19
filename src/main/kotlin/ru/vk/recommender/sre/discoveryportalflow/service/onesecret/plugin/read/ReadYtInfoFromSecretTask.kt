package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.read

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.YtInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretReadTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretService

class ReadYtInfoFromSecretTask(
    oneSecretService: OneSecretService,
    private val ytInfoOneSecretCodec: YtInfoOneSecretCodec,
) : AbstractReadOneSecretTask(oneSecretService) {

    override fun resolveRequestedKeys(taskRunContext: OneSecretReadTaskContext): List<String> {
        return ytInfoOneSecretCodec.readKeys.toList()
    }

    override fun applyResolvedSecretData(
        taskRunContext: OneSecretReadTaskContext,
        resolvedSecretData: Map<String, String>,
    ) {
        taskRunContext.ytOffline = ytInfoOneSecretCodec.fromSecretData(resolvedSecretData)
    }
}
