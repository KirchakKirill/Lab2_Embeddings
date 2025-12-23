package org.core.embedding.api

import org.core.dto.MessageData
import org.core.dto.MessageHistoryData
import org.core.dto.ToolData
import java.net.http.HttpResponse

interface ChatBotManager {
    suspend fun generateChatMessage(url: String, chatHistory: MutableList<MessageData>, model: String, tools: List<ToolData> = listOf()): HttpResponse<String>?
    suspend fun searchInternet(url: String, query: String, max_results: Int): HttpResponse<String>?
}