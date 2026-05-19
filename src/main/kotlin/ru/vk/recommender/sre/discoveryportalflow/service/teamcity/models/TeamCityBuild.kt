package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TeamCityBuild(
    val id: Long? = null,
    val buildTypeId: String? = null,
    val number: String? = null,
    val status: String? = null,
    val state: String? = null,
    val branchName: String? = null,
    val href: String? = null,
    val webUrl: String? = null,
) {
    fun isRunning(): Boolean {
        return state == STATE_RUNNING
    }

    fun isFailed(): Boolean {
        return status == STATUS_FAILURE
    }

    fun isCompleted(): Boolean {
        return state == STATE_FINISHED && status == STATUS_SUCCESS
    }

    private companion object {
        const val STATE_FINISHED = "finished"
        const val STATE_RUNNING = "running"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAILURE = "FAILURE"
    }
}

data class TriggeredBuild(
    val dockerBuildId: String,
    val branch: String,
    val queueId: String,
)
