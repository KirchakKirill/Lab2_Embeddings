package org.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultEmbedding(
    @SerialName("embedding")
    val embedding: List<Float>
)