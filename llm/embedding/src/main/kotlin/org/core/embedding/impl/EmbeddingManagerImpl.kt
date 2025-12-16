package org.core.embedding.impl

import org.core.embedding.api.EmbeddingManager
import org.core.embedding.api.EmbeddingSource
import org.core.embedding.api.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import org.core.dto.GameData
import org.core.dto.InsertData
import org.core.dto.ResultEmbedding
import java.net.http.HttpClient
import java.net.http.HttpResponse

class EmbeddingManagerImpl: EmbeddingManager {

    private val json = Json {
        ignoreUnknownKeys = true
    }
    val dispatcher  = Dispatchers.IO.limitedParallelism(10)

    private val client: NetworkClient by lazy {
        NetworkClientAdapter(
            httpClient = HttpClient.newBuilder().build()
        )
    }

    override suspend fun getOneEmbedding(url: String, source: EmbeddingSource): HttpResponse<String>? {
        return client.doRequest(url, source)
    }

    override suspend fun getNEmbedding(
        url: String,
        descriptions: List<GameData>,
        model: String
    ): List<InsertData> = coroutineScope {

        val mapperToInsertData = { response: HttpResponse<String>?, gameData: GameData ->
            response?.let {
                InsertData(
                    model = model,
                    description = gameData.description,
                    name = gameData.name,
                    released = gameData.released,
                    playtime = gameData.playtime,
                    metacritic = gameData.metacritic,
                    embedding = json.decodeFromString<ResultEmbedding>(it.body()).embedding
                )
            }
        }

        val embeddings = descriptions.map {
            async(dispatcher) {
                val embeddingForOne = getOneEmbedding(url, GameDataEmbeddingRequest(it, model))
                println("EmbeddingManager: $coroutineContext")
                mapperToInsertData(embeddingForOne, it)
            }
        }.awaitAll().filterNotNull()
        return@coroutineScope embeddings
    }
}