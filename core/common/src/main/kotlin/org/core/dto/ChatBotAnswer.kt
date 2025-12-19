package org.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatBotAnswer(
    @SerialName("model")
    val model: String,
    @SerialName("remote_model")
    val remote_model: String,
    @SerialName("remote_host")
    val remote_host: String,
    @SerialName("created_at")
    val created_at: String,
    @SerialName("message")
    val message: MessageData,
    @SerialName("done")
    val done: Boolean,
    @SerialName("done_reason")
    val done_reason: String,
    @SerialName("total_duration")
    val total_duration: Long,
    @SerialName("prompt_eval_count")
    val prompt_eval_count: Int,
    @SerialName("eval_count")
    val eval_count: Int
)