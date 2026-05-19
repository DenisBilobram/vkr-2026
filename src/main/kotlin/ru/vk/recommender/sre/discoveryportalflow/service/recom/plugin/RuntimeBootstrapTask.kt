package ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin

import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowContextTransformTask
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.RuntimeTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory

abstract class RecommenderRuntimeBootstrapTask(
    private val recomRuntimeContextFactory: RecomRuntimeContextFactory,
) : FlowContextTransformTask<BootstrapRecomContext>(BootstrapRecomContext::class) {

    protected fun buildRuntimeContext(stageContext: BootstrapRecomContext): RuntimeTaskContext {
        return recomRuntimeContextFactory.build(stageContext)
    }
}
