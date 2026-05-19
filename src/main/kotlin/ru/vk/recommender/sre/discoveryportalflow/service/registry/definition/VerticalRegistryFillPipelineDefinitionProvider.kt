package ru.vk.recommender.sre.discoveryportalflow.service.registry.definition

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextFieldValidationInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextOptionInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextTabInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineDefinitionInfo
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.project.RegistryProjectRepository
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.field.PipelineContextFieldFactory
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.pipeline.PublicPipelineDefinitionProvider
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType

@Component
class VerticalRegistryFillPipelineDefinitionProvider(
    private val projectRepository: RegistryProjectRepository,
    private val fieldFactory: PipelineContextFieldFactory,
    private val serviceTypeInfoFactory: RegistryServiceTypeInfoFactory,
    private val dataCentersTabFactory: RegistryDataCentersTabFactory,
) : PublicPipelineDefinitionProvider {
    override val pipelineName: String = PIPELINE_NAME

    override fun definition(): PipelineDefinitionInfo {
        return PipelineDefinitionInfo(
            pipelineName = pipelineName,
            label = "Fill vertical registry",
            tabs = listOf(
                generalTab(),
                servicesTab(),
                secretsTab(),
                dataCentersTabFactory.dataCentersTab(),
            ),
        )
    }

    private fun generalTab(): PipelineContextTabInfo {
        return fieldFactory.tab(
            tabName = "general",
            label = "General",
            fields = listOf(
                fieldFactory.textField(
                    name = "recommenderName",
                    path = "recommender.recommenderName",
                    label = "Vertical Name",
                    required = true,
                    placeholder = "Enter vertical name...",
                    validation = PipelineContextFieldValidationInfo(pattern = "^[a-z0-9][a-z0-9-]*[a-z0-9]$"),
                ),
                fieldFactory.intField(
                    name = "productId",
                    path = "recommender.productId",
                    label = "Product Id",
                    required = true,
                    placeholder = "Enter product id...",
                    validation = PipelineContextFieldValidationInfo(min = 1),
                ),
                fieldFactory.selectField(
                    name = "projectName",
                    path = "recommender.projectName",
                    label = "Project",
                    required = true,
                    placeholder = "Select project...",
                    options = projectOptions(),
                    allowCustomValue = true,
                ),
            ),
        )
    }

    private fun servicesTab(): PipelineContextTabInfo {
        return fieldFactory.tab(
            tabName = "services",
            label = "Services",
            fields = listOf(
                fieldFactory.textField(
                    name = "serviceOwner",
                    path = "recommender.serviceOwner",
                    label = "Service Owner",
                    required = true,
                    placeholder = "Enter service owner...",
                ),
                fieldFactory.serviceListField(
                    serviceTypes = serviceTypeInfoFactory.serviceTypeInfos(
                        requiredServiceTypes = REQUIRED_VERTICAL_SERVICE_TYPES,
                        optionalServiceTypes = OPTIONAL_VERTICAL_SERVICE_TYPES,
                    ),
                    minItems = REQUIRED_VERTICAL_SERVICE_TYPES.size,
                ),
            ),
        )
    }

    private fun secretsTab(): PipelineContextTabInfo {
        return fieldFactory.tab(
            tabName = "secrets",
            label = "Secrets",
            fields = listOf(
                fieldFactory.secretIdField(
                    name = "ytOnlineRobotSecretId",
                    path = "recommender.ytOnlineRobotSecretId",
                    label = "Yt Online Robot SecretId",
                ),
                fieldFactory.secretIdField(
                    name = "ytOfflineRobotSecretId",
                    path = "recommender.ytOfflineRobotSecretId",
                    label = "Yt Offline Robot SecretId",
                ),
                fieldFactory.secretIdField(
                    name = "redisCredentialsSecretId",
                    path = "recommender.redisCredentialsSecretId",
                    label = "Redis Credentials SecretId",
                ),
            ),
        )
    }

    private fun projectOptions(): List<PipelineContextOptionInfo> {
        return projectRepository.findAll()
            .asSequence()
            .map { project -> project.projectName }
            .sorted()
            .map { projectName -> fieldFactory.option(projectName, projectName) }
            .toList()
    }

    private companion object {
        const val PIPELINE_NAME = "VerticalRegistryFill"

        val REQUIRED_VERTICAL_SERVICE_TYPES = listOf(
            ServiceType.MEDIATOR,
            ServiceType.GATEWAY,
            ServiceType.BASE,
            ServiceType.META,
        )

        val OPTIONAL_VERTICAL_SERVICE_TYPES = listOf(
            ServiceType.FACTOR_PROXY,
            ServiceType.SNAPSHOTS_BUILDER,
            ServiceType.YT_PROXY,
        )
    }
}
