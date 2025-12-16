package org.core.dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HTTPResuestData(
    @SerialName("desc")
    val desc:String,
    @SerialName("metric")
    val metric:String,
    @SerialName("model")
    val model:String
)