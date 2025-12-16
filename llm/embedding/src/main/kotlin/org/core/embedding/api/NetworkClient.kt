package org.core.embedding.api

import java.net.http.HttpResponse

interface NetworkClient {
   suspend fun doRequest(url: String, source: EmbeddingSource): HttpResponse<String>?
}