package org.core.embedding.api

import org.core.dto.RequestData

interface EmbeddingSource {
    fun toRequestBody(): RequestData
}