package org.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageData(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String
)
