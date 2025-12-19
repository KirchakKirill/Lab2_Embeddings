package org.core.embedding.api

import org.core.dto.MessageData
import org.core.dto.MessageHistoryData
import java.net.http.HttpResponse

interface NetworkClient {
   suspend fun doRequest(url: String, source: EmbeddingSource): HttpResponse<String>?
   suspend fun generateChatMessage(url: String, source: MutableList<MessageData>, model: String): HttpResponse<String>?
}