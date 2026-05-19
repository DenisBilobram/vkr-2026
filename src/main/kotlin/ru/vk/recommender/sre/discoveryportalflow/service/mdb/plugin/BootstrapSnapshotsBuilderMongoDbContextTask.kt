package ru.vk.recommender.sre.discoveryportalflow.service.mdb.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.context.MdbStageTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.context.MdbTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.service.MdbMongoCredentialsFactory
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretWriteTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin.RecommenderRuntimeBootstrapTask
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory

class BootstrapSnapshotsBuilderMongoDbContextTask(
    private val mdbMongoCredentialsFactory: MdbMongoCredentialsFactory,
    recomRuntimeContextFactory: RecomRuntimeContextFactory,
) : RecommenderRuntimeBootstrapTask(recomRuntimeContextFactory) {

    override suspend fun executeCasted(taskRunContext: BootstrapRecomContext): TaskRunResult {
        val snapshotsBuilderRuntime = resolveSnapshotsBuilderRuntime(taskRunContext)

        runtimeLogger.info(
            "Bootstrapped MDB context for snapshots-builder ${snapshotsBuilderRuntime.cloudServiceName}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapRecomContext): MdbStageTaskContext {
        val snapshotsBuilderRuntime = resolveSnapshotsBuilderRuntime(taskRunContext)
        val mongoCredentials = mdbMongoCredentialsFactory.create(snapshotsBuilderRuntime.cloudServiceName)
        val snapshotsBuilderSecretId = resolveSnapshotsBuilderSecretId(
            taskRunContext = taskRunContext,
            cloudServiceName = snapshotsBuilderRuntime.cloudServiceName,
        )

        return MdbStageTaskContext(
            mdbTaskContext = MdbTaskContext(
                databaseName = mongoCredentials.databaseName,
                userName = mongoCredentials.userName,
                userPassword = mongoCredentials.userPassword,
            ),
            oneSecretWriteTaskContext = OneSecretWriteTaskContext(
                secretId = snapshotsBuilderSecretId,
                secretData = mongoCredentials.secretData,
                comment = "Update snapshots-builder MongoDB credentials for ${mongoCredentials.databaseName}",
            ),
        )
    }

    private fun resolveSnapshotsBuilderSecretId(
        taskRunContext: BootstrapRecomContext,
        cloudServiceName: String,
    ): String {
        return requireNotNull(taskRunContext.serviceOneSecretOutcomes[cloudServiceName]?.sharedSecretId) {
            "MDB provisioning requires OneSecret service secret for $cloudServiceName"
        }
    }

    private fun resolveSnapshotsBuilderRuntime(taskRunContext: BootstrapRecomContext) =
        buildRuntimeContext(taskRunContext).services.singleOrNull { serviceRuntime ->
            serviceRuntime.type == ServiceType.SNAPSHOTS_BUILDER
        } ?: error("MDB provisioning requires enabled ${ServiceType.SNAPSHOTS_BUILDER.name} service")
}
