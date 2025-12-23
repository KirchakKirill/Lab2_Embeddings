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
    val stream: Boolean,
    @SerialName("tools")
    val tools: List<ToolData> = listOf()
)

@Serializable
data class ToolData(
    @SerialName("type")
    val type: String,
    @SerialName("function")
    val function: FunctionData
)

@Serializable
data class FunctionData(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("parameters")
    val parameters: ParametersData,
)

@Serializable
data class ParametersData(
    @SerialName("type")
    val type: String,
    @SerialName("properties")
    val properties: PropertiesDataWebSearch,
    @SerialName("required")
    val required: List<String>,
)

@Serializable
data class PropertiesDataWebSearch(
    @SerialName("url")
    val url: UrlData,
    @SerialName("query")
    val query: QueryData? = null,
    @SerialName("max_results")
    val max_results: MaxResultsData? = null,
    @SerialName("model")
    val model: ModelData? = null,
    @SerialName("metric")
    val metric: MetricData? = null,
    @SerialName("prompt")
    val prompt: PromptData? = null
)

@Serializable
data class UrlData(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String = "The URL where the request should be sent to get the search result."
)

@Serializable
data class QueryData(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String = "The search query string."
)

@Serializable
data class MaxResultsData(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String = "Maximum results to return."
)

@Serializable
data class ModelData(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String = "Model for creating vector representations (embeddings) of data."
)

@Serializable
data class MetricData(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String = "A metric that determines the closeness of vector representations (embeddings)."
)

@Serializable
data class PromptData(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String = "Data to be converted into vector representation (embedding)."
)
