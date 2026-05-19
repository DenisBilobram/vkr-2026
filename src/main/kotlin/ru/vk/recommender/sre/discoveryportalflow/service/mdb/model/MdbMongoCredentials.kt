package ru.vk.recommender.sre.discoveryportalflow.service.mdb.model

data class MdbMongoCredentials(
    val databaseName: String,
    val userName: String,
    val userPassword: String,
    val secretData: Map<String, String>,
)
