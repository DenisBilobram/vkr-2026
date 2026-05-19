package ru.vk.recommender.sre.discoveryportalflow.service.mdb.client

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.config.MdbProperties
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.CreateMdbDatabaseRequest
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.CreateMdbUserRequest
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbOperationResponse
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbUserSummary

class MdbClient(
    private val objectMapper: ObjectMapper,
    private val mdbProperties: MdbProperties,
) {

    fun createDatabase(request: CreateMdbDatabaseRequest): MdbOperationResponse {
        ndaStub("createDatabase")
        return MdbOperationResponse(id = "nda", status = "REMOVED")
    }

    fun createUser(request: CreateMdbUserRequest): MdbOperationResponse {
        ndaStub("createUser")
        return MdbOperationResponse(id = "nda", status = "REMOVED")
    }

    fun listOperations(): List<MdbOperationResponse> {
        ndaStub("listOperations")
        return emptyList()
    }

    fun listUsers(): List<MdbUserSummary> {
        ndaStub("listUsers")
        return emptyList()
    }

    private fun ndaStub(operation: String) {
        // NDA code removed: production implementation calls an internal managed database API.
        objectMapper.createObjectNode().put("operation", operation).put("baseUrl", mdbProperties.baseUrl)
    }
}
