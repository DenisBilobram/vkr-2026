package ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowContextTransformTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapProjectRecommenderContextTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ProjectRecommenderRuntimeContextFactory

class ProjectRecommenderRuntimeBootstrapTask(
    private val projectRecommenderRuntimeContextFactory: ProjectRecommenderRuntimeContextFactory,
) : FlowContextTransformTask<BootstrapProjectRecommenderContextTaskContext>(BootstrapProjectRecommenderContextTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: BootstrapProjectRecommenderContextTaskContext): TaskRunResult {
        runtimeLogger.info("Bootstrapped project runtime context for ${taskRunContext.projectRecommender.projectName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapProjectRecommenderContextTaskContext): FlowTaskContext {
        return projectRecommenderRuntimeContextFactory.build(taskRunContext)
    }
}
