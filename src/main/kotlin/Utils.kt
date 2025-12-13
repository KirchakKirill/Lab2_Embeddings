package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.data.GameData
import org.example.data.GameInfo
import org.example.data.LLM
import java.io.FileReader

object Utils {
    suspend fun getDescriptionFromFile(path:String):String? = withContext(Dispatchers.IO)
    {
        val file  = FileReader(path)
        var description:String? = null
        try {
            file.use {
                description =  it.readText()
            }
            return@withContext description
        }
        catch (e: Exception){
            println("Erorr: ${e.message}")
            return@withContext null
        }
    }

    fun mapperGameData(games:List<GameInfo?>):List<GameData> {
        return games.mapNotNull { game ->
            with(game) {
                takeIf { listOf(
                    this?.name,
                    this?.description,
                    this?.released,
                    this?.playtime,
                    this?.metacritic
                ).all { it != null } }?.let { g->
                    GameData(
                        name = g.name!!,
                        description = g.description!!,
                        playtime = g.playtime!!,
                        released = g.released!!,
                        metacritic = g.metacritic!!
                    )
                }
            }
        }
    }

    fun tableForModel(model: String): String? {
        when(model) {
            LLM.NOMIC.key -> {
               return LLM.NOMIC.tableName
            }
            LLM.SNOWFLAKE.key -> {
               return LLM.SNOWFLAKE.tableName
            }
            LLM.MXBAI.key -> {
               return LLM.MXBAI.tableName
            }
            else -> return null
        }
    }
}