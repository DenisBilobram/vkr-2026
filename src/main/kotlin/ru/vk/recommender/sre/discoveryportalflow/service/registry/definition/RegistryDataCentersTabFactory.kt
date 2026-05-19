package ru.vk.recommender.sre.discoveryportalflow.service.registry.definition

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextFieldValidationInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextTabInfo
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.field.PipelineContextFieldFactory
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings

@Component
class RegistryDataCentersTabFactory(
    private val fieldFactory: PipelineContextFieldFactory,
) {

    fun dataCentersTab(): PipelineContextTabInfo {
        val defaults = RecommenderDcSettings()
        val datacenterOptions = DatacenterCode.entries.map { datacenterCode ->
            fieldFactory.option(datacenterCode.name, datacenterCode.name.uppercase())
        }

        return fieldFactory.tab(
            tabName = "dataCenters",
            label = "Data Centers",
            fields = listOf(
                fieldFactory.multiSelectField(
                    name = "productionDcs",
                    path = "dcSettings.productionDcs",
                    label = "Production DCs",
                    required = true,
                    defaultValue = defaults.productionDcs,
                    placeholder = "Add DC...",
                    options = datacenterOptions,
                    validation = PipelineContextFieldValidationInfo(minItems = 1, maxItems = 3),
                ),
                fieldFactory.multiSelectField(
                    name = "canaryDcs",
                    path = "dcSettings.canaryDcs",
                    label = "Canary DC",
                    required = true,
                    defaultValue = defaults.canaryDcs,
                    placeholder = "Select DC...",
                    options = datacenterOptions,
                    validation = PipelineContextFieldValidationInfo(minItems = 1, maxItems = 1),
                ),
                fieldFactory.multiSelectField(
                    name = "testingDcs",
                    path = "dcSettings.testingDcs",
                    label = "Testing DC",
                    required = true,
                    defaultValue = defaults.testingDcs,
                    placeholder = "Select DC...",
                    options = datacenterOptions,
                    validation = PipelineContextFieldValidationInfo(minItems = 1, maxItems = 1),
                ),
            ),
        )
    }
}
