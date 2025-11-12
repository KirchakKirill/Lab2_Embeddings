package org.example.manager

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.example.data.GameInfo
import org.example.data.RequestData

class GameManager
{
    companion object {
        const val model = "nomic-embed-text:v1.5"
    }

    private val key = System.getenv("KEY")

    private val httpClient: OkHttpClient

    init {
        this.httpClient = OkHttpClient.Builder().addInterceptor {
                chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url

            val newUrl = originalUrl.newBuilder()
                .addQueryParameter("key", key)
                .build()

            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }.build()
    }

    suspend fun run(url:String): GameInfo?  = withContext(Dispatchers.IO) {
        try {
            val response = doRequest(url)
            response.body?.let { body ->
                val responseData = body.string()

                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true

                }
                val decoded = json.decodeFromString<GameInfo>(responseData)
                println(coroutineContext)
                return@withContext decoded
            }
            return@withContext null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("Error during make request: ${e.message}")
            null
        }
    }

    fun doRequest(url: String): Response {
         val request  = Request.Builder()
             .url(url)
             .build()
         val response = httpClient.newCall(request).execute()
         return response
    }

    suspend fun getNDescriptions(url: String, n:Int):List<GameInfo> = coroutineScope {
        val range = IntRange(1, n)
        val Dresult =
            range.map {
                async {
                    run(url + "$it")
                }
            }.awaitAll().filterNotNull()
        return@coroutineScope Dresult
    }

    fun generateRequestData(gamesDescriptions:List<GameInfo?>, modelType: String):List<RequestData> {
        return gamesDescriptions.mapNotNull { if(it?.description == null) null else RequestData(
            model = modelType,
            prompt = it.description
        )
        }
    }

}