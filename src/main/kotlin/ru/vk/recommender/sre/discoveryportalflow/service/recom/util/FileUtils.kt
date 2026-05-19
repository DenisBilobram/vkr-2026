package ru.vk.recommender.sre.discoveryportalflow.service.recom.util

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

fun buildBaseReplacements(recommenderName: String): Map<String, String> {
    return buildNamedReplacements("RecomName", recommenderName)
}

fun buildNamedReplacements(
    placeholderPrefix: String,
    rawName: String,
): Map<String, String> {
    val names = parseNames(rawName)
    return mapOf(
        placeholder("${placeholderPrefix}Package") to names.packageName,
        placeholder("${placeholderPrefix}Class") to names.className,
        placeholder("${placeholderPrefix}Var") to names.variableName,
        placeholder("${placeholderPrefix}Enum") to names.enumName,
        placeholder("${placeholderPrefix}Folder") to names.folderName,
        placeholder(placeholderPrefix) to rawName,
    )
}

fun renderTemplate(raw: String, replacements: Map<String, String>): String {
    var result = raw
    replacements.forEach { (placeholder, value) ->
        result = result.replace(placeholder, value)
    }
    return result
}

private fun placeholder(name: String): String {
    return "\${$name}"
}

fun writeText(path: Path, content: String) {
    Files.createDirectories(path.parent)
    Files.writeString(path, content, StandardCharsets.UTF_8)
}

