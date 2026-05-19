package ru.vk.recommender.sre.discoveryportalflow.persistence.model

enum class FlowStatus {
    PENDING,
    READY,
    RUNNING,
    WAITING,
    BLOCKED,
    SUCCEEDED,
    FAILED,
    FAILED_WITH_RETRY,
    CANCELED,
    SKIPPED
}
