package org.example

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.example.data.InsertData
import org.example.data.LLM
import org.example.data.RequestData
import org.example.data.ResultEmbedding
import org.example.db.DistanceMetric
import org.example.db.VectorDB
import org.example.manager.DatabaseMigrator
import org.example.manager.GameManager
import org.example.manager.HttpRestHandler
import org.example.manager.EmbeddingManager
import java.net.InetSocketAddress

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

//var nomicEmbeddingManager: NomicEmbeddingManager? = null

fun main(): kotlin.Unit = runBlocking {
    DatabaseMigrator.migrate()
    if (!DatabaseMigrator.isMigrated) {
        return@runBlocking
    }

    val manager = GameManager()
    val games = manager.getNDescriptions("https://api.rawg.io/api/games/",100)

    val info = Utils.mapperGameData(games)
    val info_snowflake = Utils.mapperGameData(games)
    val info_mxbai = Utils.mapperGameData(games)


    val embeddingManager = EmbeddingManager("http://localhost:11434/api/embeddings")

    val embeddings = embeddingManager.getNEmbedding(info, LLM.NOMIC.key)
    //val embeddings_snowflake = embeddingManager.getNEmbedding(info_snowflake, LLM.SNOWFLAKE.key)
    //val embeddings_mxbai = embeddingManager.getNEmbedding(info_mxbai, LLM.MXBAI.key)
    val embeddings_snowflake = listOf<InsertData>()
    val embeddings_mxbai = listOf<InsertData>()

    VectorDB.preloadData(listOf(embeddings, embeddings_snowflake, embeddings_mxbai).flatten(), LLM.NOMIC.key
    ) { cur, total -> println("Обработано $cur записей из $total...") }
    val  text = Utils.getDescriptionFromFile("1.txt")

    processText(text, DistanceMetric.INNER_PRODUCT, LLM.NOMIC.key, embeddingManager)
    //initHttpServer(embeddingManager)
}

fun initHttpServer(embeddingManager: EmbeddingManager) {
    println("Init http server ...")
    val server = HttpServer.create(InetSocketAddress(8088), 0)
    server.createContext("/games", HttpRestHandler(embeddingManager))
    server.executor = null // creates a default executor
    server.start()
    println("Init http server ... done")
}

private suspend fun processText(text: String?, metricType: DistanceMetric?, modelType: String, embeddingManager: EmbeddingManager) : MutableList<String> {
    val result: MutableList<String> = mutableListOf<String>()
    text?.let {
        val emb = embeddingManager.getEmbeddingOneGame( // получаем embedding пользовательского описания
            RequestData(
                model = modelType,
                prompt = text
            )
        )
        println(emb?.body())
        if (emb?.body() != null ){
            val resEmb = Json.decodeFromString<ResultEmbedding>(emb.body()) // десериализуем
            val res =  VectorDB.getNeighbors(resEmb.embedding, metricType!!, modelType) // получаем пять ближайших соседей
            // DistanceMetric - функции расстояния
            res?.forEachIndexed { index, item ->
                println("$index: name: ${item.name}\n" +
                        "playtime: ${item.playtime}\n" +
                        "released: ${item.released}\n" +
                        "metacritic: ${item.metacritic}\n" +
                        "description: ${item.description}\n")
                //result.add(item.orEmpty().replace("\"", "").replace("'", ""))
            }
        }
    }
    return result
}


