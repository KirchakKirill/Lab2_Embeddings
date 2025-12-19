package org.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageHistoryData (
    @SerialName("model")
    val model: String,
    @SerialName("messages")
    val messages: List<MessageData>,
    @SerialName("stream")
    val stream: Boolean
)