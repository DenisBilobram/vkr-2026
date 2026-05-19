package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model

data class OneSecretQueueTargetPlan(
    val verticalTargets: List<OneSecretQueueTarget>,
    val projectTargets: List<OneSecretQueueTarget>,
    val serviceTargets: List<ServiceOneSecretTargetPlan>,
)
