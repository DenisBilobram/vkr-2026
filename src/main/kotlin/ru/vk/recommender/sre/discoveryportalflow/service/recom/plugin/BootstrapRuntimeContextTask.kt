package ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.RuntimeTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory

class BootstrapRuntimeContextTask(
    recomRuntimeContextFactory: RecomRuntimeContextFactory,
) : RecommenderRuntimeBootstrapTask(recomRuntimeContextFactory) {

    override suspend fun executeCasted(taskRunContext: BootstrapRecomContext): TaskRunResult {
        runtimeLogger.info("Bootstrapped runtime context for ${taskRunContext.recommender.recommenderName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapRecomContext): RuntimeTaskContext {
        return buildRuntimeContext(taskRunContext)
    }
}
