package org.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebSearchRequest(
    @SerialName("query")
    val query: String,
    @SerialName("max_results")
    val max_results: Int
)
