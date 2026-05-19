package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.field

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextFieldInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextFieldValidationInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextInputType
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextOptionInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineContextTabInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineServiceListContextFieldInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineServiceTypeInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineSimpleContextFieldInfo

@Component
class PipelineContextFieldFactory {

    fun tab(
        tabName: String,
        label: String,
        fields: List<PipelineContextFieldInfo>,
    ): PipelineContextTabInfo = PipelineContextTabInfo(
        tabName = tabName,
        label = label,
        fields = fields,
    )

    fun textField(
        name: String,
        path: String,
        label: String,
        required: Boolean = false,
        placeholder: String? = null,
        defaultValue: Any? = null,
        validation: PipelineContextFieldValidationInfo? = null,
    ): PipelineContextFieldInfo = PipelineSimpleContextFieldInfo(
        name = name,
        path = path,
        label = label,
        inputType = PipelineContextInputType.TEXT,
        required = required,
        placeholder = placeholder,
        defaultValue = defaultValue,
        validation = validation,
    )

    fun secretIdField(
        name: String,
        path: String,
        label: String,
    ): PipelineContextFieldInfo = textField(
        name = name,
        path = path,
        label = label,
        placeholder = "src-...",
    )

    fun intField(
        name: String,
        path: String,
        label: String,
        required: Boolean = false,
        placeholder: String? = null,
        defaultValue: Any? = null,
        validation: PipelineContextFieldValidationInfo? = null,
    ): PipelineContextFieldInfo = PipelineSimpleContextFieldInfo(
        name = name,
        path = path,
        label = label,
        inputType = PipelineContextInputType.INTEGER,
        required = required,
        placeholder = placeholder,
        defaultValue = defaultValue,
        validation = validation,
    )

    fun checkboxField(
        name: String,
        path: String,
        label: String,
        defaultValue: Boolean,
    ): PipelineContextFieldInfo = PipelineSimpleContextFieldInfo(
        name = name,
        path = path,
        label = label,
        inputType = PipelineContextInputType.CHECKBOX,
        defaultValue = defaultValue,
    )

    fun selectField(
        name: String,
        path: String,
        label: String,
        required: Boolean = false,
        placeholder: String? = null,
        defaultValue: Any? = null,
        options: List<PipelineContextOptionInfo>,
        allowCustomValue: Boolean = false,
    ): PipelineContextFieldInfo = PipelineSimpleContextFieldInfo(
        name = name,
        path = path,
        label = label,
        inputType = PipelineContextInputType.SELECT,
        required = required,
        placeholder = placeholder,
        defaultValue = defaultValue,
        allowCustomValue = allowCustomValue,
        options = options,
    )

    fun multiSelectField(
        name: String,
        path: String,
        label: String,
        required: Boolean = false,
        placeholder: String? = null,
        defaultValue: Any? = null,
        options: List<PipelineContextOptionInfo>,
        validation: PipelineContextFieldValidationInfo? = null,
    ): PipelineContextFieldInfo = PipelineSimpleContextFieldInfo(
        name = name,
        path = path,
        label = label,
        inputType = PipelineContextInputType.MULTI_SELECT,
        required = required,
        placeholder = placeholder,
        defaultValue = defaultValue,
        options = options,
        validation = validation,
    )

    fun serviceListField(
        serviceTypes: List<PipelineServiceTypeInfo>,
        minItems: Int,
        name: String = "services",
        path: String = "services",
        label: String = "Services",
    ): PipelineContextFieldInfo {
        val duplicatedTypes = serviceTypes
            .groupBy { serviceType -> serviceType.type }
            .filterValues { entries -> entries.size > 1 }
            .keys
        require(duplicatedTypes.isEmpty()) {
            "Service list field contains duplicated service types: ${duplicatedTypes.joinToString()}"
        }

        return PipelineServiceListContextFieldInfo(
            name = name,
            path = path,
            label = label,
            required = true,
            validation = PipelineContextFieldValidationInfo(minItems = minItems),
            serviceTypes = serviceTypes.map { serviceType ->
                val servicePath = "$path.${serviceType.type}"
                serviceType.copy(
                    path = servicePath,
                    selectionField = serviceType.selectionField.copy(path = servicePath),
                    fields = serviceType.fields.map { field ->
                        when (field) {
                            is PipelineSimpleContextFieldInfo -> field.copy(path = "$servicePath.${field.path}")
                            is PipelineServiceListContextFieldInfo -> field
                        }
                    },
                )
            },
        )
    }

    fun serviceTypeInfo(
        type: String,
        label: String,
        required: Boolean,
        defaultValue: Map<String, Any?>,
        fields: List<PipelineContextFieldInfo> = emptyList(),
    ): PipelineServiceTypeInfo = PipelineServiceTypeInfo(
        type = type,
        label = label,
        required = required,
        defaultEnabled = required,
        selectionField = serviceSelectionField(
            type = type,
            label = label,
            required = required,
        ),
        defaultValue = defaultValue,
        fields = fields,
    )

    private fun serviceSelectionField(
        type: String,
        label: String,
        required: Boolean,
    ): PipelineSimpleContextFieldInfo = PipelineSimpleContextFieldInfo(
        name = "selected",
        path = type,
        label = label,
        inputType = PipelineContextInputType.CHECKBOX,
        required = required,
        defaultValue = required,
    )

    fun option(value: String, label: String): PipelineContextOptionInfo {
        return PipelineContextOptionInfo(value = value, label = label)
    }
}
