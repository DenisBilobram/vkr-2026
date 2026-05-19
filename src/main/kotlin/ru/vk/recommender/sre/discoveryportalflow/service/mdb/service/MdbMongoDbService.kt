package ru.vk.recommender.sre.discoveryportalflow.service.mdb.service

import ru.vk.recommender.sre.discoveryportalflow.service.mdb.client.MdbClient
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.config.MdbProperties
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.CreateMdbDatabaseRequest
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.CreateMdbUserRequest
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbDatabaseMongoSettings
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbDatabaseSettings
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbMongoAclParams
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbMongoRoleBinding
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbMongoRoles
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbUpsertUserParams
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbUserAclParams

class MdbMongoDbService(
    private val mdbClient: MdbClient,
    private val mdbProperties: MdbProperties,
) {

    fun createDatabase(databaseName: String): String {
        val operation = mdbClient.createDatabase(
            CreateMdbDatabaseRequest(
                name = databaseName,
                settings = MdbDatabaseSettings(
                    mongodbSettings = MdbDatabaseMongoSettings(
                        availableRoles = listOf(mdbProperties.userRole),
                    ),
                ),
            ),
        )
        return operation.id
    }

    fun createUser(
        databaseName: String,
        userName: String,
        userPassword: String,
    ): String {
        val operation = mdbClient.createUser(
            CreateMdbUserRequest(
                aclParams = MdbUserAclParams(
                    name = userName,
                    password = userPassword,
                    upsertUserParams = MdbUpsertUserParams(
                        mongoAclParams = MdbMongoAclParams(
                            roles = MdbMongoRoles(
                                mongodbRoles = listOf(
                                    MdbMongoRoleBinding(
                                        roles = listOf(mdbProperties.userRole),
                                        dbName = databaseName,
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
        return operation.id
    }

    fun findOperation(operationId: String) = mdbClient.listOperations()
        .firstOrNull { operation -> operation.id == operationId }

    companion object {
        const val STATUS_DONE = "done"
        const val STATUS_FAILED = "failed"
        const val STATUS_CANCELED = "canceled"
    }
}
