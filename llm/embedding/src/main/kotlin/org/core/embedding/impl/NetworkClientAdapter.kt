package org.core.embedding.impl

import org.core.embedding.api.EmbeddingSource
import org.core.embedding.api.NetworkClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.core.dto.MessageData
import org.core.dto.MessageHistoryData
import org.core.extensions.toUri
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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
        model: String
    ): HttpResponse<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val body = Json.Default.encodeToString(MessageHistoryData(model, source, false))
                val request: HttpRequest = HttpRequest.newBuilder()
                    .uri(url.toUri())
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