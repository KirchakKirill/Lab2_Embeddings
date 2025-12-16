package org.example

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.core.db.api.DatabaseProvider
import org.core.dto.DistanceMetric
import org.core.dto.GameInfo
import org.core.dto.InsertData
import org.core.dto.LLM
import org.core.dto.RequestData
import org.core.dto.ResultEmbedding
import org.core.embedding.api.EmbeddingManager
import org.core.embedding.impl.UserEmbeddingRequest
import org.core.mapper.Mapper
import org.core.network.api.ClientProvider
import java.net.InetSocketAddress
import org.core.network.api.HandlerProcessor
import org.core.network.factory.HttpHandlerFactory
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.


lateinit var embeddingManager: EmbeddingManager
lateinit var clientProvider: ClientProvider
lateinit var databaseProvider: DatabaseProvider

const val embeddingUrl =  "http://localhost:11434/api/embeddings"

val processor = object : HandlerProcessor {
    override suspend fun process(
        text: String?,
        metric: DistanceMetric?,
        model: String
    ): List<String> {
        val result: MutableList<String> = mutableListOf()
        text?.let {
            val emb = embeddingManager.getOneEmbedding(embeddingUrl,
                UserEmbeddingRequest(
                    RequestData(
                        model = model,
                        prompt = text
                    )
                )
            )

            if (emb?.body() != null ){
                val resEmb = Json.decodeFromString<ResultEmbedding>(emb.body()) // десериализуем
                val res =  databaseProvider.database.databaseClient.databaseQuery.getNeighbors(resEmb.embedding, metric!!, model) // получаем пять ближайших соседей
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

}

var isMigrationsDone = false

fun main(): Unit = runBlocking {

    startKoin {
        modules(appModule)
        printLogger()
    }

    databaseProvider = get(DatabaseProvider::class.java)
    embeddingManager = get(EmbeddingManager::class.java)
    clientProvider = get(ClientProvider::class.java)

    //1
    isMigrationsDone = databaseProvider.database.databaseMigrator.migrate()
    if  (!isMigrationsDone) {
        return@runBlocking
    }
    //2
    val games:List<GameInfo> =  clientProvider.client.dataSource.getNRequest("https://api.rawg.io/api/games/",100)

    //3
    val info = Mapper.mapperGameData(games)
    val info_snowflake = Mapper.mapperGameData(games)
    val info_mxbai = Mapper.mapperGameData(games)

    //4
    val embeddings = embeddingManager.getNEmbedding(embeddingUrl, info, LLM.NOMIC.key)
    //val embeddings_snowflake = embeddingManager.getNEmbedding(info_snowflake, LLM.SNOWFLAKE.key)
    //val embeddings_mxbai = embeddingManager.getNEmbedding(info_mxbai, LLM.MXBAI.key)
    val embeddings_snowflake = listOf<InsertData>()
    val embeddings_mxbai = listOf<InsertData>()

    //5
    val initializer = databaseProvider.database.databaseClient.dbInitializer
    initializer.preloadData(listOf(embeddings, embeddings_snowflake, embeddings_mxbai).flatten(), LLM.NOMIC.key
    ) { cur, total -> println("Обработано $cur записей из $total...") }
    val  text = Utils.getDescriptionFromFile("1.txt")

    processor.process(text, DistanceMetric.INNER_PRODUCT, LLM.NOMIC.key)
    //initHttpServer()
}

fun initHttpServer() {
    println("Init http server ...")
    val server = HttpServer.create(InetSocketAddress(8088), 0)
    server.createContext("/games",
        HttpHandlerFactory.Builder()
        .withProcessor(processor)
        .build())
    server.executor = null // creates a default executor
    server.start()
    println("Init http server ... done")
}




