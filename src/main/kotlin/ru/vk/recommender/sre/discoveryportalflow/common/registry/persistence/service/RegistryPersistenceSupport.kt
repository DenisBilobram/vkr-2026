package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service

import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode

internal const val REGISTRY_EMPTY_JSON = "{}"
internal const val REGISTRY_DEFAULT_CLOUD_NAMESPACE = "public"

internal fun Collection<String>.toRegistryTextArray(): Array<String> = distinct().toTypedArray()

internal fun Collection<String>.toRegistryDatacenterCodes(): List<DatacenterCode> =
    distinct()
        .map(DatacenterCode::fromValue)
