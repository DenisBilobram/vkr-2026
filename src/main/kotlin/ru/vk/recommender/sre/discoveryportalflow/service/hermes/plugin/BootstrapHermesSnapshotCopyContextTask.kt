package ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.context.HermesSnapshotCopyTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.service.HermesSnapshotTypeResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin.RecommenderRuntimeBootstrapTask
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory

class BootstrapHermesSnapshotCopyContextTask(
    recomRuntimeContextFactory: RecomRuntimeContextFactory,
    private val hermesSnapshotTypeResolver: HermesSnapshotTypeResolver,
) : RecommenderRuntimeBootstrapTask(recomRuntimeContextFactory) {

    override suspend fun executeCasted(taskRunContext: BootstrapRecomContext): TaskRunResult {
        runtimeLogger.info(
            "Bootstrapped Hermes snapshot copy context for ${taskRunContext.recommender.recommenderName}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapRecomContext): HermesSnapshotCopyTaskContext {
        val runtimeContext = buildRuntimeContext(taskRunContext)
        val baseServiceConfig = taskRunContext.services
            .firstOrNull { it.type == ServiceType.BASE && !it.serviceDisabled }
            ?.requireConfig(BaseServiceConfig::class)
            ?: error("Hermes snapshot copy requires enabled Base service config")

        return HermesSnapshotCopyTaskContext(
            runtime = runtimeContext,
            baseShardsCount = baseServiceConfig.shardsCount,
            snapshotTypeIds = hermesSnapshotTypeResolver.resolveSnapshotTypeIds(
                recommenderName = runtimeContext.recommenderName,
                baseShardsCount = baseServiceConfig.shardsCount,
            ),
        )
    }
}
