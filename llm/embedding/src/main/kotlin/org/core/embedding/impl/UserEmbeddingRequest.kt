package org.core.embedding.impl

import org.core.embedding.api.EmbeddingSource
import org.core.dto.RequestData

data class UserEmbeddingRequest(
    val requestData: RequestData
) : EmbeddingSource {
    override fun toRequestBody(): RequestData {
        return  requestData
    }
}