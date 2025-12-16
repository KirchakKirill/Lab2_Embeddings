package org.core.embedding.impl

import org.core.embedding.api.EmbeddingSource
import org.core.dto.GameData
import org.core.dto.RequestData
import org.core.extensions.mapperToRequestData

data class GameDataEmbeddingRequest(
    val gameData: GameData,
    val model: String
) : EmbeddingSource {
    override fun toRequestBody(): RequestData {
        return gameData.mapperToRequestData(model)
    }
}