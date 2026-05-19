package ru.vk.recommender.sre.discoveryportalflow.api.dto.flow

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PipelineContextTabInfo(
    val tabName: String,
    val label: String,
    val fields: List<PipelineContextFieldInfo>,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
sealed interface PipelineContextFieldInfo {
    val name: String
    val path: String
    val label: String
    val inputType: PipelineContextInputType
    val required: Boolean
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PipelineSimpleContextFieldInfo(
    override val name: String,
    override val path: String,
    override val label: String,
    override val inputType: PipelineContextInputType,
    override val required: Boolean = false,
    val placeholder: String? = null,
    val defaultValue: Any? = null,
    val allowCustomValue: Boolean = false,
    val options: List<PipelineContextOptionInfo> = emptyList(),
    val validation: PipelineContextFieldValidationInfo? = null,
) : PipelineContextFieldInfo

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PipelineServiceListContextFieldInfo(
    override val name: String,
    override val path: String,
    override val label: String,
    override val required: Boolean = true,
    val validation: PipelineContextFieldValidationInfo? = null,
    val serviceTypes: List<PipelineServiceTypeInfo>,
) : PipelineContextFieldInfo {
    override val inputType: PipelineContextInputType = PipelineContextInputType.SERVICE_LIST
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PipelineServiceTypeInfo(
    val type: String,
    val path: String = type,
    val label: String,
    val required: Boolean = false,
    val defaultEnabled: Boolean = false,
    val selectionField: PipelineSimpleContextFieldInfo,
    val defaultValue: Map<String, Any?> = emptyMap(),
    val fields: List<PipelineContextFieldInfo> = emptyList(),
)

data class PipelineContextOptionInfo(
    val value: String,
    val label: String,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PipelineContextFieldValidationInfo(
    val min: Int? = null,
    val max: Int? = null,
    val minItems: Int? = null,
    val maxItems: Int? = null,
    val pattern: String? = null,
)

enum class PipelineContextInputType(
    @JsonValue val value: String,
) {
    TEXT("text"),
    INTEGER("integer"),
    CHECKBOX("checkbox"),
    SELECT("select"),
    MULTI_SELECT("multiSelect"),
    SERVICE_LIST("serviceList"),
}
