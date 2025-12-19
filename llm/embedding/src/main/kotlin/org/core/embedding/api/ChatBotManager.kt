package org.core.embedding.api

import org.core.dto.MessageData
import org.core.dto.MessageHistoryData
import java.net.http.HttpResponse

interface ChatBotManager {
    suspend fun generateChatMessage(url: String, chatHistory: MutableList<MessageData>, model: String): HttpResponse<String>?
}