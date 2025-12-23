package org.core.embedding.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.core.dto.MessageData
import org.core.dto.MessageHistoryData
import org.core.dto.ToolData
import org.core.embedding.api.ChatBotManager
import org.core.embedding.api.NetworkClient
import java.net.http.HttpClient
import java.net.http.HttpResponse

class ChatBotManagerImpl : ChatBotManager {

    private val client: NetworkClient by lazy {
        NetworkClientAdapter(
            httpClient = HttpClient.newBuilder().build()
        )
    }

    override suspend fun generateChatMessage(url: String, chatHistory: MutableList<MessageData>, model: String, tools: List<ToolData>): HttpResponse<String>? {
        return client.generateChatMessage(url, chatHistory, model, tools)
    }

    override suspend fun searchInternet(url: String, query: String, max_results: Int): HttpResponse<String>? {
        return client.searchInternet(url, query, max_results)
    }
}