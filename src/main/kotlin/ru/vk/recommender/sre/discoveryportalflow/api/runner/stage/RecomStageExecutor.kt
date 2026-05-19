package ru.vk.recommender.sre.discoveryportalflow.api.runner.stage

import com.fasterxml.jackson.databind.JsonNode
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.runner.client.FlowInvocationHttpClient
import ru.vk.recommender.sre.discoveryportalflow.api.runner.client.FlowQueryHttpClient

class RecomStageExecutor(
    private val flowInvocationHttpClient: FlowInvocationHttpClient,
    private val flowQueryHttpClient: FlowQueryHttpClient,
) {

    fun runServicehostSetting(stageTasksContext: JsonNode) {
        runStage(RecomStageName.SERVICEHOST_SETTING, stageTasksContext)
    }

    fun runDictionaryProjectSetting(stageTasksContext: JsonNode) {
        runStage(RecomStageName.DICTIONARY_PROJECT_SETTING, stageTasksContext)
    }

    fun runVerticalGeneration(stageTasksContext: JsonNode) {
        runStage(RecomStageName.RUNTIME_VERTICAL_GENERATION, stageTasksContext)
    }

    fun runPrepareRecomInfra(stageTasksContext: JsonNode) {
        runStage(RecomStageName.PREPARE_RECOM_INFRA, stageTasksContext)
    }

    fun runPmsSetting(stageTasksContext: JsonNode) {
        runStage(RecomStageName.PMS_SETTING, stageTasksContext)
    }

    fun runOneSecretSetting(stageTasksContext: JsonNode) {
        runStage(RecomStageName.ONESECRET_SETTING, stageTasksContext)
    }

    fun runMdbMongoSettings(stageTasksContext: JsonNode) {
        runStage(RecomStageName.MDB_MONGO_SETTINGS, stageTasksContext)
    }

    fun runOtherSetting(stageTasksContext: JsonNode) {
        runStage(RecomStageName.OTHER_SETTING, stageTasksContext)
    }

    fun runServicesGeneration(stageTasksContext: JsonNode) {
        runStage(RecomStageName.SERVICES_GENERATION, stageTasksContext)
    }

    fun runServicesOnecloudDeployment(stageTasksContext: JsonNode) {
        runStage(RecomStageName.SERVICES_ONECLOUD_DEPLOYMENT, stageTasksContext)
    }

    fun runOnlineTogglesTenantCreate(stageTasksContext: JsonNode) {
        runStage(RecomStageName.TOGGLES_ONLINE_TENANT_CREATE, stageTasksContext)
    }

    fun runOnlineTogglesTenantUpdate(stageTasksContext: JsonNode) {
        runStage(RecomStageName.TOGGLES_ONLINE_TENANT_UPDATE, stageTasksContext)
    }

    fun runOfflineTogglesTenantCreate(stageTasksContext: JsonNode) {
        runStage(RecomStageName.TOGGLES_OFFLINE_TENANT_CREATE, stageTasksContext)
    }

    fun runOfflineTogglesTenantUpdate(stageTasksContext: JsonNode) {
        runStage(RecomStageName.TOGGLES_OFFLINE_TENANT_UPDATE, stageTasksContext)
    }

    fun runHermesSnaphotsSettings(stageTasksContext: JsonNode) {
        runStage(RecomStageName.HERMES_SNAPHOTS_SETTINGS, stageTasksContext)
    }

    fun runHermerSnapshotsSync(stageTasksContext: JsonNode) {
        runStage(RecomStageName.HERMER_SNAPSHOTS_SYNC, stageTasksContext)
    }

    private fun runStage(stageName: RecomStageName, stageTasksContext: JsonNode) {
        val pipelineRun = flowInvocationHttpClient.createPipeline(
            pipelineName = stageName.pipelineName,
            pipelineContext = stageTasksContext,
        )
        val pipelineRunDetails = flowQueryHttpClient.getPipelineRunDetails(pipelineRun.pipelineRunId)
        val stageRunId = requireNotNull(findStageRunId(pipelineRunDetails, stageName.stageName)) {
            "Stage '${stageName.stageName}' was not materialized in pipeline '${stageName.pipelineName}'"
        }
        print("Got stage run '$stageRunId'")
        flowInvocationHttpClient.startStage(stageRunId)
    }

    private fun findStageRunId(
        pipelineRunDetails: PipelineRunDetailsResponse,
        stageName: String,
    ): java.util.UUID? {
        pipelineRunDetails.stages.firstOrNull { stageRun -> stageRun.stageName == stageName }?.let { stageRun ->
            return stageRun.id
        }
        return null
    }
}
