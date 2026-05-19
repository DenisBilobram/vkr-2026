package ru.vk.recommender.sre.discoveryportalflow.config

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import java.util.Base64

object TlsFileLoader {

    fun loadKeyStoreFromBase64File(
        path: String,
        password: CharArray,
        type: String,
    ): KeyStore {
        return KeyStore.getInstance(type).apply {
            ByteArrayInputStream(readBase64Decoded(path)).use { inputStream ->
                load(inputStream, password)
            }
        }
    }

    private fun readBase64Decoded(path: String): ByteArray {
        val encoded = Files.readString(Path.of(path), StandardCharsets.UTF_8)
        require(encoded.isNotBlank()) {
            "TLS file $path is empty"
        }

        return try {
            Base64.getMimeDecoder().decode(encoded)
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException(
                "TLS file $path must contain Base64-encoded certificate data",
                exception,
            )
        }
    }
}
