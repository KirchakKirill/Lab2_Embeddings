package org.example

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.*
import org.example.data.HTTPResuestData
import org.example.data.RequestData
import org.example.data.ResponseData
import org.example.data.ResultEmbedding
import org.example.db.DistanceMetric
import org.example.db.VectorDB
import org.example.manager.GameManager
import org.example.manager.HttpRestHandler
import org.example.manager.NomicEmbeddingManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

//var nomicEmbeddingManager: NomicEmbeddingManager? = null

fun main(): kotlin.Unit = runBlocking {

    val manager = GameManager()
    val games = manager.getNDescriptions("https://api.rawg.io/api/games/",1000)

    val info = manager.generateRequestData(games, "nomic-embed-text:v1.5")
    val info_snowflake = manager.generateRequestData(games, "snowflake-arctic-embed2")
    val info_mxbai = manager.generateRequestData(games, "mxbai-embed-large")


    val nomicEmbeddingManager = NomicEmbeddingManager("http://localhost:11434/api/embeddings")
    //nomicEmbeddingManager = NomicEmbeddingManager("http://localhost:11434/api/embeddings")
    val embeddings = nomicEmbeddingManager.getNEmbedding(info)
    val embeddings_snowflake = nomicEmbeddingManager.getNEmbedding(info_snowflake)
    val embeddings_mxbai = nomicEmbeddingManager.getNEmbedding(info_mxbai)


    VectorDB.createTableEmbedding()
    VectorDB.preloadData(embeddings, "MXGAMES")
    VectorDB.preloadData(embeddings_snowflake, "MXGAMESSNOWFLAKE")
    VectorDB.preloadData(embeddings_mxbai, "MXGAMESMXBAI")

    initHttpServer(nomicEmbeddingManager)

    //val nomicEmbeddingManager = createTable()
    //val nomicEmbeddingManager  = NomicEmbeddingManager("http://localhost:11434/api/embeddings")


/*    val text = Utils.getDescriptionFromFile("1.txt") // здесь мы считываем пользовательское описание,
    // которое будет сравниваться на похожесть с уже сохраненными
    println(text)
    text?.let {
        val emb = nomicEmbeddingManager.getEmbeddingOneGame( // получаем embedding пользовательского описания
            RequestData(
                model = "nomic-embed-text:v1.5",
                prompt = text
            )
        )
        println(emb?.body())
        if (emb?.body() != null ){
            val resEmb = Json.decodeFromString<ResultEmbedding>(emb.body()) // десериализуем
            val res =  VectorDB.getNeighbors(resEmb.embedding, DistanceMetric.INNER_PRODUCT) // получаем пять ближайших соседей
                // DistanceMetric - функции расстояния
            res?.forEachIndexed { index, item -> println("$index: $item")}
        }
    }*/

}

fun initHttpServer(nomicEmbeddingManager: NomicEmbeddingManager) {
    println("Init http server ...")
    val server = HttpServer.create(InetSocketAddress(8088), 0)
    server.createContext("/games", HttpRestHandler(nomicEmbeddingManager))
    server.executor = null // creates a default executor
    server.start()
    println("Init http server ... done")
}

suspend fun processText(text: String?, metricType: DistanceMetric?, modelType: String, nomicEmbeddingManager: NomicEmbeddingManager) : MutableList<String> {
    // которое будет сравниваться на похожесть с уже сохраненными
    //println(text)
    var result: MutableList<String> = mutableListOf<String>()
    text?.let {
        val emb = nomicEmbeddingManager!!.getEmbeddingOneGame( // получаем embedding пользовательского описания
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
                println("$index: $item")
                result.add(item.orEmpty().replace("\"", "").replace("'", ""))
            }
        }
    }
    return result
}


/*
* createTable - функция, которая создает таблицу с embedding и заполняет ее собственно embedding'ами,
* которые мы получаем путем отправки описания игры модели 'nomic-embed-text:v1.5'
* Steps:
* 1.getNDescriptions - получаем n описаний игр
* 2.generateRequestData - преобразуем в класс данных, который дальше отправляем в качестве body в POST запросе
* 3.getNEmbedding - отправляем N запросов к языковой модели, чтоб из описаний сделать embedding
* 4.createTableEmbedding - создаем пустую таблицу, поля: id, description,embedding
* 5.preloadData  - полученные на шаге 3 embeddings сохраняем в БД
 */
suspend fun createTable(): NomicEmbeddingManager{
    val manager = GameManager()
    val games = manager.getNDescriptions("https://api.rawg.io/api/games/",100)
    val info = manager.generateRequestData(games, "nomic-embed-text:v1.5")


    val nomicEmbeddingManager  = NomicEmbeddingManager("http://localhost:11434/api/embeddings")
    val embeddings = nomicEmbeddingManager.getNEmbedding(info)


    VectorDB.createTableEmbedding()
    VectorDB.preloadData(embeddings, "MXGAMES")
    return nomicEmbeddingManager
}


