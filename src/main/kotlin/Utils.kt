package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
}