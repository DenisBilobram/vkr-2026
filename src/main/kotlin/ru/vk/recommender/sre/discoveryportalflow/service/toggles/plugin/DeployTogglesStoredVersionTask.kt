package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.service.TogglesTenantSupport

class DeployTogglesStoredVersionTask(
    private val togglesClient: TogglesClient,
    private val togglesTenantSupport: TogglesTenantSupport,
) : FlowTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        val version = requireNotNull(taskRunContext.togglesStoredVersion) {
            "Toggles stored version is required for tenant ${taskRunContext.tenantName}"
        }
        val recommenderDeployTargets = togglesTenantSupport.buildDeployTargets(taskRunContext.dcs)
        val tenantDcs = togglesClient.getTenantDcs(taskRunContext.tenantName)
            .map(String::lowercase)
            .distinct()
        val tenantDcSet = tenantDcs.toSet()
        val deployTargets = recommenderDeployTargets.filter { deployTarget -> deployTarget in tenantDcSet }

        require(deployTargets.isNotEmpty()) {
            "No common Toggles deploy targets for tenant ${taskRunContext.tenantName}: " +
                "recommender targets=$recommenderDeployTargets, tenant dcs=$tenantDcs"
        }

        val deployTargetSet = deployTargets.toSet()
        val skippedRecommenderDeployTargets = recommenderDeployTargets - deployTargetSet
        val tenantOnlyDcs = tenantDcs - deployTargetSet
        if (skippedRecommenderDeployTargets.isNotEmpty() || tenantOnlyDcs.isNotEmpty()) {
            runtimeLogger.warn(
                "Toggles deploy targets for tenant ${taskRunContext.tenantName} are not fully matched: " +
                    "deploy targets=$deployTargets, skipped recommender targets=$skippedRecommenderDeployTargets, " +
                    "tenant-only dcs=$tenantOnlyDcs, recommender targets=$recommenderDeployTargets, tenant dcs=$tenantDcs",
            )
        }

        deployTargets.forEach { deployTarget ->
            togglesClient.deployVersion(taskRunContext.tenantName, version, deployTarget)
        }
        runtimeLogger.info(
            "Deployed Toggles tenant ${taskRunContext.tenantName} version $version to $deployTargets",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
