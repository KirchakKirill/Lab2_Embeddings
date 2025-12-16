package org.core.embedding.api

import org.core.dto.GameData
import org.core.dto.InsertData
import java.net.http.HttpResponse

interface EmbeddingManager {
    suspend fun getOneEmbedding(url: String, source: EmbeddingSource): HttpResponse<String>?
    suspend fun getNEmbedding(url: String, descriptions: List<GameData>, model:String ):List<InsertData>
}
