package org.example.manager

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.example.data.GameData
import org.example.data.InsertData
import org.example.data.RequestData
import org.example.data.ResultEmbedding
import org.example.mapperToRequestData
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class EmbeddingManager(private val path:String)
{
    private var url: URI
    private val httpClient: HttpClient

    init {
        this.url = URI.create(this.path)
        this.httpClient = HttpClient.newBuilder().build()
    }

    private suspend fun getEmbeddingOneGame(gameData: GameData, model: String): HttpResponse<String>? {
       return doRequest(null, gameData, model)
    }

    suspend fun getEmbeddingOneGame(requestData: RequestData): HttpResponse<String>? {
        return doRequest(requestData,null,null)
    }

    private suspend fun doRequest(requestData: RequestData?=null, gameData: GameData?=null, model: String?=null): HttpResponse<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val data = if (gameData != null && model != null) gameData.mapperToRequestData(model) else requestData
                val body = Json.Default.encodeToString(data)
                val request: HttpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build()
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                println("EmbeddingManager[withContext]: $coroutineContext")
                return@withContext response
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Error during request embedding")
                null
            }
        }
    }

    suspend fun getNEmbedding(descriptions: List<GameData>, model:String ):List<InsertData> = coroutineScope {
        fun mapperToInsertData(response: HttpResponse<String>?, gameData: GameData): InsertData? {
            if (response != null) {
                val json = Json {
                    ignoreUnknownKeys = true
                }

                return InsertData(
                    model = model,
                    description = gameData.description,
                    name = gameData.name,
                    released = gameData.released,
                    playtime = gameData.playtime,
                    metacritic = gameData.metacritic,
                    embedding = json.decodeFromString<ResultEmbedding>(response.body()).embedding
                )
            }
            return null
        }

        val embeddings = descriptions.map {
            async(Dispatchers.IO.limitedParallelism(10)) {
                val embeddingForOne = getEmbeddingOneGame(it, model)
                println("EmbeddingManager: $coroutineContext")
                mapperToInsertData(embeddingForOne, it)
            }
        }.awaitAll().filterNotNull()
        return@coroutineScope embeddings
    }
}