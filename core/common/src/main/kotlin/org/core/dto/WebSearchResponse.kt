package org.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebSearchResponse(
    @SerialName("results")
    val results: List<OneResult>
)

@Serializable
data class OneResult(
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String,
    @SerialName("content")
    val content: String
)
