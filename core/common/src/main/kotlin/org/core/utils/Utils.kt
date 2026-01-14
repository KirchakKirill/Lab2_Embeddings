package org.core.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileReader
import org.core.dto.LLM
import java.io.File

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

    fun addRecordToFile(path:String, description: String)
    {
        val file = File(path)
        try {
            file.appendText(description, Charsets.UTF_8)
        }
        catch (e: Exception){
            println("Erorr: ${e.message}")
        }
    }

    fun tableForModel(model: String): String? {
        return when(model) {
            LLM.NOMIC.key -> {
                LLM.NOMIC.tableName
            }

            LLM.SNOWFLAKE.key -> {
                LLM.SNOWFLAKE.tableName
            }

            LLM.MXBAI.key -> {
                LLM.MXBAI.tableName
            }
            else -> null
        }
    }

    fun parseDescription(description: String): String {
        return description.replace(Regex("<[^>]*>"), "")
    }
}