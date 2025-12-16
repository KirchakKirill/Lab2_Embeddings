package org.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestData(
    @SerialName("model")
    val model:String,
    @SerialName("prompt")
    val prompt:String
)