package org.core.embedding.impl

import org.core.embedding.api.EmbeddingSource
import org.core.embedding.api.NetworkClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.core.dto.MessageData
import org.core.dto.MessageHistoryData
import org.core.dto.ToolData
import org.core.dto.WebSearchRequest
import org.core.extensions.toUri
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class NetworkClientAdapter(
    private val httpClient: HttpClient
): NetworkClient {

    override suspend fun doRequest(
        url: String,
        source: EmbeddingSource
    ): HttpResponse<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val body = Json.Default.encodeToString(source.toRequestBody())
                val request: HttpRequest = HttpRequest.newBuilder()
                    .uri(url.toUri())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build()
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                return@withContext response
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Error during request embedding")
                null
            }
        }
    }

    override suspend fun generateChatMessage(
        url: String,
        source: MutableList<MessageData>,
        model: String,
        tools: List<ToolData>
    ): HttpResponse<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val body = Json.Default.encodeToString(MessageHistoryData(model, source, false, tools))
                val request: HttpRequest = HttpRequest.newBuilder()
                    .uri(url.toUri())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(3600))
                    .build()
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                return@withContext response
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Error during request chat message")
                null
            }
        }
    }

    override suspend fun searchInternet(
        url: String,
        query: String,
        max_results: Int
    ): HttpResponse<String>? {
        val ollamaApiKey = System.getenv("OLLAMA_API_KEY")

        return withContext(Dispatchers.IO) {
            try {
                val body = Json.Default.encodeToString(WebSearchRequest(query, max_results))
                val request: HttpRequest = HttpRequest.newBuilder()
                    .uri(url.toUri())
                    .headers("Authorization", "Bearer $ollamaApiKey")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build()
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                return@withContext response
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Error during request chat message")
                null
            }
        }
    }
}