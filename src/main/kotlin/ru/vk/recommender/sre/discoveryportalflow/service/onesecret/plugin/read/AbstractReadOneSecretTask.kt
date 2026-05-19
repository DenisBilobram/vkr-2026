package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.read

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretReadTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretService

abstract class AbstractReadOneSecretTask(
    private val oneSecretService: OneSecretService,
) : FlowTask<OneSecretReadTaskContext>(OneSecretReadTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OneSecretReadTaskContext): TaskRunResult {
        val requestedKeys = resolveRequestedKeys(taskRunContext)
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
        require(requestedKeys.isNotEmpty()) {
            "No OneSecret keys requested for secretId=${taskRunContext.secretId}"
        }

        val resolvedSecretData = oneSecretService.getSecretData(
            secretId = taskRunContext.secretId,
            requestedKeys = requestedKeys,
        )

        taskRunContext.secretData.putAll(resolvedSecretData)
        applyResolvedSecretData(taskRunContext, resolvedSecretData)

        runtimeLogger.info(
            "Loaded ${resolvedSecretData.size} key(s) from OneSecret ${taskRunContext.secretId}: " +
                resolvedSecretData.keys.joinToString(", "),
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    protected abstract fun resolveRequestedKeys(taskRunContext: OneSecretReadTaskContext): List<String>

    protected open fun applyResolvedSecretData(
        taskRunContext: OneSecretReadTaskContext,
        resolvedSecretData: Map<String, String>,
    ) = Unit
}
