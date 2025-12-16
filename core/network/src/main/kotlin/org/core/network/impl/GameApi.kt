package org.core.network.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.Response
import org.core.network.api.DataSource
import org.core.dto.GameInfo
import org.core.network.api.NetworkClient

internal class GameApi(private val networkClient: NetworkClient): DataSource {

    override suspend fun getOneRequest(url:String): GameInfo?  = withContext(Dispatchers.IO) {

        fun doRequest(): Response {
            val request  = Request.Builder()
                .url(url)
                .build()
            val response =  networkClient.execute(request)
            return response
        }

        try {
            val response = doRequest()
            response.body?.let { body ->
                val responseData = body.string()

                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true

                }
                val decoded = json.decodeFromString<GameInfo>(responseData)
                println("GameManager[withContext]: $coroutineContext")
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

   override suspend fun getNRequest(url: String, n:Int):List<GameInfo> = coroutineScope{
        val range = IntRange(1, n)
        val dResult =
            range.map {
                async {
                    println("GameManager: $coroutineContext")
                    getOneRequest(url + "$it")
                }
            }.awaitAll().filterNotNull()
        return@coroutineScope dResult
    }
}