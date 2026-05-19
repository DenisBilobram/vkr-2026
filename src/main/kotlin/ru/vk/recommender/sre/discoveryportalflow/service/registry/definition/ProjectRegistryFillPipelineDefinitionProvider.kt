package ru.vk.recommender.sre.discoveryportalflow.service.registry.definition

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextFieldValidationInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextTabInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineDefinitionInfo
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.field.PipelineContextFieldFactory
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.pipeline.PublicPipelineDefinitionProvider
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType

@Component
class ProjectRegistryFillPipelineDefinitionProvider(
    private val fieldFactory: PipelineContextFieldFactory,
    private val serviceTypeInfoFactory: RegistryServiceTypeInfoFactory,
    private val dataCentersTabFactory: RegistryDataCentersTabFactory,
) : PublicPipelineDefinitionProvider {
    override val pipelineName: String = PIPELINE_NAME

    override fun definition(): PipelineDefinitionInfo {
        return PipelineDefinitionInfo(
            pipelineName = pipelineName,
            label = "Fill project registry",
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
                    name = "projectName",
                    path = "projectRecommender.projectName",
                    label = "Project Name",
                    required = true,
                    placeholder = "Enter project name...",
                    validation = PipelineContextFieldValidationInfo(pattern = "^[a-z0-9][a-z0-9-]*[a-z0-9]$"),
                ),
                fieldFactory.intField(
                    name = "productId",
                    path = "projectRecommender.productId",
                    label = "Product Id",
                    required = true,
                    placeholder = "Enter product id...",
                    validation = PipelineContextFieldValidationInfo(min = 1),
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
                    path = "projectRecommender.serviceOwner",
                    label = "Service Owner",
                    required = true,
                    placeholder = "Enter service owner...",
                ),
                fieldFactory.serviceListField(
                    serviceTypes = serviceTypeInfoFactory.serviceTypeInfos(
                        requiredServiceTypes = REQUIRED_PROJECT_SERVICE_TYPES,
                        optionalServiceTypes = OPTIONAL_PROJECT_SERVICE_TYPES,
                    ),
                    minItems = REQUIRED_PROJECT_SERVICE_TYPES.size,
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
                    path = "projectRecommender.ytOnlineRobotSecretId",
                    label = "Yt Online Robot Secret Id",
                ),
                fieldFactory.secretIdField(
                    name = "ytOfflineRobotSecretId",
                    path = "projectRecommender.ytOfflineRobotSecretId",
                    label = "Yt Offline Robot Secret Id",
                ),
            ),
        )
    }

    private companion object {
        const val PIPELINE_NAME = "ProjectRegistryFill"

        val REQUIRED_PROJECT_SERVICE_TYPES = listOf(
            ServiceType.GRPC_PROXY,
        )

        val OPTIONAL_PROJECT_SERVICE_TYPES = listOf(
            ServiceType.SNAPSHOTS_BUILDER,
            ServiceType.FACTOR_PROXY,
            ServiceType.YT_PROXY,
        )
    }
}
