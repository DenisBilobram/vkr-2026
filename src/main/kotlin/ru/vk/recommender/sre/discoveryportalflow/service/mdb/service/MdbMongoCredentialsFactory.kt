package ru.vk.recommender.sre.discoveryportalflow.service.mdb.service

import ru.vk.recommender.sre.discoveryportalflow.service.mdb.model.MdbMongoCredentials
import java.security.SecureRandom

class MdbMongoCredentialsFactory {
    private val secureRandom = SecureRandom()

    fun create(databaseName: String): MdbMongoCredentials {
        val userName = "${databaseName}_user"
        val userPassword = generatePassword()

        return MdbMongoCredentials(
            databaseName = databaseName,
            userName = userName,
            userPassword = userPassword,
            secretData = linkedMapOf(
                "$databaseName.bazinga.mongo.auth_user" to userName,
                "$databaseName.bazinga.mongo.auth_password" to userPassword,
            ),
        )
    }

    private fun generatePassword(length: Int = DEFAULT_PASSWORD_LENGTH): String {
        return buildString(length) {
            repeat(length) {
                append(PASSWORD_ALPHABET[secureRandom.nextInt(PASSWORD_ALPHABET.length)])
            }
        }
    }

    private companion object {
        private const val DEFAULT_PASSWORD_LENGTH = 16
        private const val PASSWORD_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }
}
