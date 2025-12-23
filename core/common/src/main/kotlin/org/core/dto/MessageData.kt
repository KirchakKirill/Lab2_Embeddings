package org.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageData(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String,
    @SerialName("tool_calls")
    val tool_calls: List<OneTool> = listOf(),
    @SerialName("tool_name")
    val tool_name: String? = null
)

@Serializable
data class OneTool(
    @SerialName("id")
    val id: String,
    @SerialName("function")
    val function: FunctionDataRequest
)

@Serializable
data class FunctionDataRequest(
    @SerialName("index")
    val index: Int,
    @SerialName("name")
    val name: String,
    @SerialName("arguments")
    val arguments: ArgumentsData
)

@Serializable
data class ArgumentsData(
    @SerialName("url")
    val url: String,
    @SerialName("query")
    val query: String? = null,
    @SerialName("max_results")
    val max_results: Int? = null,
    @SerialName("model")
    val model: String? = null,
    @SerialName("metric")
    val metric: String? = null,
    @SerialName("prompt")
    val prompt: String? = null
)
