package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec

internal const val SECRET_PLACEHOLDER = "placeholder"

internal data class OneSecretField<InfoType>(
    val key: String,
    private val valueProvider: (InfoType) -> String,
) {
    fun entry(source: InfoType): Pair<String, String> {
        return key to valueProvider(source)
    }

    fun requiredValue(secretData: Map<String, String>): String {
        return optionalValue(secretData)
            ?: error("Missing required OneSecret key '$key'")
    }

    fun optionalValue(secretData: Map<String, String>): String? {
        return secretData[key]
    }
}

internal fun <InfoType> buildOneSecretMapping(
    source: InfoType,
    fields: Iterable<OneSecretField<InfoType>>,
): MutableMap<String, String> {
    return fields.associateTo(linkedMapOf()) { field ->
        field.entry(source)
    }
}

internal fun <InfoType> buildOneSecretKeySet(
    fields: Iterable<OneSecretField<InfoType>>,
): Set<String> {
    return fields.mapTo(linkedSetOf()) { field ->
        field.key
    }
}
