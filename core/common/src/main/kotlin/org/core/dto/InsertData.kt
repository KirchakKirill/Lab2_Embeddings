package org.core.dto

data class InsertData (
    val model: String,
    val playtime:Int,
    val name: String,
    val description:String,
    val metacritic:Int,
    val released:String,
    val embedding: List<Float>
)