package ru.vk.recommender.sre.discoveryportalflow.service.mdb.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class CreateMdbDatabaseRequest(
    val name: String,
    val settings: MdbDatabaseSettings,
)

data class MdbDatabaseSettings(
    val mongodbSettings: MdbDatabaseMongoSettings,
)

data class MdbDatabaseMongoSettings(
    val availableRoles: List<String>,
)

data class CreateMdbUserRequest(
    val aclParams: MdbUserAclParams,
)

data class MdbUserAclParams(
    val name: String,
    val password: String,
    val upsertUserParams: MdbUpsertUserParams,
)

data class MdbUpsertUserParams(
    val mongoAclParams: MdbMongoAclParams,
)

data class MdbMongoAclParams(
    val roles: MdbMongoRoles,
)

data class MdbMongoRoles(
    val mongodbRoles: List<MdbMongoRoleBinding>,
)

data class MdbMongoRoleBinding(
    val roles: List<String>,
    val dbName: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MdbOperationResponse(
    val id: String,
    val status: String,
    val errorMessage: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MdbUserSummary(
    val name: String,
)
