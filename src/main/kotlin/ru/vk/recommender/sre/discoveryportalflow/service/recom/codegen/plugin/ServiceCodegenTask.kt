package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabCommitDraft
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabMergeRequestDraft
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.context.ServicesGenerationTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.generator.ProtoGenerator
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.validator.ServiceTemplateCoverageValidator
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.enabledServices
import java.text.SimpleDateFormat
import java.util.Date

class ServiceCodegenTask(
    private val gitlabClient: GitlabClient,
    private val protoGenerator: ProtoGenerator,
    private val serviceTemplateCoverageValidator: ServiceTemplateCoverageValidator,
    private val serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
) : FlowTask<ServicesGenerationTaskContext>(ServicesGenerationTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: ServicesGenerationTaskContext): TaskRunResult {
        val recommenderRuntime = taskRunContext.recommender
        val recommenderName = recommenderRuntime.recommenderName

        val gitlabProjectClient = GitlabProjectClient(gitlabClient, gitlabClient.getRepositoryProjectId())

        protoGenerator.generateProtoApis(recommenderRuntime, gitlabProjectClient)
        runtimeLogger.info("Generated proto API files for $recommenderName")

        protoGenerator.updateRecommenderType(recommenderRuntime, gitlabProjectClient)
        runtimeLogger.info("Updated RecommenderType for $recommenderName")

        serviceTemplateCoverageValidator.validateTemplateCoverage()
        taskRunContext.services.enabledServices().forEach { serviceRuntime ->
            generateServiceCode(
                service = serviceRuntimeDefinitionResolver.service(serviceRuntime),
                taskContext = taskRunContext,
                serviceRuntime = serviceRuntime,
                gitlabProjectClient = gitlabProjectClient
            )
        }

        runtimeLogger.info("Generated service files for $recommenderName")

        val branch =
            "${recommenderRuntime.names.folderName}_fish_creation_${SimpleDateFormat("dd_mm_yyyy__hh_mm_ss").format(Date())}"
        val message = "${taskRunContext.fishJiraTask}: Создание рыбы $recommenderName"

        val commitDraft = GitlabCommitDraft(
            projectId = gitlabClient.getRepositoryProjectId(),
            branch = branch,
            commitMessage = message,
            files = gitlabProjectClient.getFiles(),
        )
        taskRunContext.gitlabCommitTaskContext.prepareDraft(commitDraft)

        val mergeRequestDraft = GitlabMergeRequestDraft(
            projectId = gitlabClient.getRepositoryProjectId(),
            sourceBranch = branch,
            title = message,
        )
        taskRunContext.gitlabMergeRequestTaskContext.prepareDraft(mergeRequestDraft)
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    private fun <TConfig : RecommenderServiceConfig> generateServiceCode(
        service: RecomService<TConfig>,
        taskContext: ServicesGenerationTaskContext,
        serviceRuntime: ServiceRuntime,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        val serviceConfig = serviceRuntime.config.requireConfig(service.configClass)
        service.generateCode(
            recommenderRuntime = taskContext.recommender,
            dcSettings = taskContext.dcSettings,
            serviceRuntime = serviceRuntime,
            serviceConfig = serviceConfig,
            gitlabProjectClient = gitlabProjectClient,
            serviceSecretOutcome = taskContext.serviceOneSecretOutcomes[serviceRuntime.cloudServiceName],
        )
    }
}
