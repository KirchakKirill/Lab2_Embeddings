package org.core.network.api

import org.core.dto.GameInfo

interface DataSource {
    suspend fun getOneRequest(url:String): GameInfo?
    suspend fun getNRequest(url: String, n:Int):List<GameInfo>
}