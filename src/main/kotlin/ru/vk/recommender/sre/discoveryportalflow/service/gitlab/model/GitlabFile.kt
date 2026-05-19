package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model

/**
 * @author mikh.nikiforov
 */
data class GitlabFile(
    val path: String,
    val mode: String = "100644",
    var content: String? = null,
    val existOk: Boolean = true,
    val mustExist: Boolean = false
)
