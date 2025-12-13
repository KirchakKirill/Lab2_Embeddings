package org.example.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameInfo(
    @SerialName("id")
    val id: Int?=null,
    @SerialName("slug")
    val slug: String?=null,
    @SerialName("name")
    val name: String?=null,
    @SerialName("name_original")
    val nameOriginal:String?=null,
    @SerialName("description")
    val description:String?=null,
    @SerialName("metacritic")
    val metacritic:Int?=null,
    @SerialName("released")
    val released:String?=null,
    @SerialName("playtime")
    val playtime:Int?=null,
)