package org.example.manager

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.example.data.InsertData
import org.example.data.RequestData
import org.example.data.ResultEmbedding
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class NomicEmbeddingManager(private val path:String)
{
    private var url: URI
    private val httpClient: HttpClient

    init {
        this.url = URI.create(this.path)
        this.httpClient = HttpClient.newBuilder().build()
    }

      suspend fun getEmbeddingOneGame(data: RequestData): HttpResponse<String>? = withContext(Dispatchers.IO) {

          try {
              val body = Json.Default.encodeToString(data)
              val request: HttpRequest = HttpRequest.newBuilder()
                  .uri(url)
                  .POST(HttpRequest.BodyPublishers.ofString(body))
                  .build()
              val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
              println(coroutineContext)
              return@withContext response
          } catch (e: CancellationException) {
              throw e
          } catch (e: Exception) {
              println("Error during request embedding")
              null
          }

      }

    suspend fun  getNEmbedding(descriptions: List<RequestData>):List<InsertData> = coroutineScope {

        val embeddings = descriptions.map {
            async {
                val emb = getEmbeddingOneGame(it)

                if (emb != null) {
                    val json = Json {
                        ignoreUnknownKeys = true
                    }
                    InsertData(
                        description = it.prompt,
                        embedding = json.decodeFromString<ResultEmbedding>(emb.body()).embedding
                    )
                } else {
                    null
                }

            }
        }.awaitAll().filterNotNull()
        return@coroutineScope embeddings
    }
}