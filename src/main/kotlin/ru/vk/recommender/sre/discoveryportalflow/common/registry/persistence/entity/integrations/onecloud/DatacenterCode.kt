package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud

import com.fasterxml.jackson.annotation.JsonCreator

enum class DatacenterCode(
) {
    pc,
    uc,
    hc,
    rc,
    kc,
    dc,
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): DatacenterCode =
            entries.firstOrNull { datacenterCode -> datacenterCode.name.equals(value, ignoreCase = true) }
                ?: error("Unsupported registry datacenter code: $value")
    }
}
