package ru.vk.recommender.sre.discoveryportalflow.service.yt.model

import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames

object YtMockTablePathResolver {

    fun resolve(
        rootPath: String,
        projectName: String,
        recommenderName: String,
        embeddingVersion: Int = 1,
    ): YtMockTablePaths {
        val recommenderRoot = joinPath(
            rootPath,
            parseNames(projectName).folderName,
            parseNames(recommenderName).folderName,
        )
        val mockRoot = joinPath(recommenderRoot, "mock")

        return YtMockTablePaths(
            root = mockRoot,
            tmp = joinPath(recommenderRoot, "tmp"),
            candidates = joinPath(mockRoot, "candidates"),
            itemFactors = joinPath(mockRoot, "item_factors"),
            itemEmbeddings = joinPath(mockRoot, "item_embeddings_v$embeddingVersion"),
        )
    }

    private fun joinPath(root: String, vararg parts: String): String {
        return (listOf(root.trimEnd('/')) + parts.map { it.trim('/') }).joinToString("/")
    }
}
