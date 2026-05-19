package ru.vk.recommender.sre.discoveryportalflow.service.ocs.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.ocs.context.OcsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.ocs.service.DictionaryProjectService

class DictionaryProjectTask(
    private val dictionaryProjectService: DictionaryProjectService,
) : FlowTask<OcsTaskContext>(OcsTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OcsTaskContext): TaskRunResult {
        dictionaryProjectService.initializeDictionaryProject(taskRunContext)
        runtimeLogger.info("Initialized dictionary project for ${taskRunContext.recommenderName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
