package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.update

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretWriteTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretService

class UpdateSecretValuesTask(
    private val oneSecretService: OneSecretService,
) : FlowTask<OneSecretWriteTaskContext>(OneSecretWriteTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OneSecretWriteTaskContext): TaskRunResult {
        val secretId = taskRunContext.secretId.trim()
        if (secretId.isEmpty()) {
            runtimeLogger.info(
                "Skip OneSecret update because secretId is blank. Prepared keys: " +
                    taskRunContext.secretData.keys.joinToString(", "),
            )
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        oneSecretService.updateSecretData(
            secretId = secretId,
            secretData = taskRunContext.secretData,
            comment = taskRunContext.comment,
        )
        runtimeLogger.info(
            "Updated OneSecret $secretId with ${taskRunContext.secretData.size} key(s): " +
                taskRunContext.secretData.keys.joinToString(", "),
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
