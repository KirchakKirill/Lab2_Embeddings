package org.example

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.core.db.api.DatabaseProvider
import org.core.dto.*
import org.core.embedding.api.ChatBotManager
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
lateinit var chatBotManager: ChatBotManager

const val embeddingUrl =  "http://localhost:11434/api/embeddings"
const val generateChatMessageUrl =  "http://localhost:11434/api/chat"

val chatHistory: MutableList<MessageData> = mutableListOf()

val processor = object : HandlerProcessor {

    override suspend fun process(
        text: String?,
        metric: DistanceMetric?,
        model: String,
        llm: LLM?
    ): List<String> {
        val result: MutableList<String> = mutableListOf()
        text?.let {
            chatHistory.add(MessageData("user", text))
            var generateLLMAnswer = chatBotManager.generateChatMessage(generateChatMessageUrl, chatHistory, llm!!.key)
            var llmAnswer: ChatBotAnswer? = null
            var summaryDescriptionForLLM: String = ""

            if (generateLLMAnswer?.body() != null) {
                try {
                    llmAnswer = Json.decodeFromString<ChatBotAnswer>(generateLLMAnswer.body()) // десериализуем
                    chatHistory.add(MessageData(llmAnswer.message.role, llmAnswer.message.content))
                }
                catch (e: Exception) {
                    println(e.toString())
                    return result
                }
            }

            if (!llmAnswer!!.message.content.contains("&&&SUMMARYDESC")) {
                result.add(llmAnswer!!.message.content)
                return result
            }

            val signature = llmAnswer!!.message.content.indexOf("&&&SUMMARYDESC", 0)
            val desriptionByLLM = llmAnswer!!.message.content.substring("&&&SUMMARYDESC".length + signature, llmAnswer!!.message.content.length)

            val emb = embeddingManager.getOneEmbedding(embeddingUrl,
                UserEmbeddingRequest(
                    RequestData(
                        model = model,
                        prompt = desriptionByLLM
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
                    summaryDescriptionForLLM +=
                        "\n---------------game-----------------\n" +
                        "Name: " + item.name +
                        "Playtime: " + item.playtime.toString() +
                        "Released: " + item.released +
                        "Metacritic: " + item.metacritic.toString()+
                        "Description: " + item.description
                    //result.add(item.orEmpty().replace("\"", "").replace("'", ""))
                }
            }

            generateLLMAnswer = chatBotManager.generateChatMessage(generateChatMessageUrl, mutableListOf(MessageData("user","Посоветуй что-то на основе приведнных далее игр. Пиши на русском языке. Выдай ответ с правильными и красивыми html тегами (цвета - очень нежный зеленый оттенок, для родительского div НЕ задавай фона вообще, цвет шрифтов должен быть темным), например, название игр, релиз и так далее выдели жирным цветом. Игры: $summaryDescriptionForLLM")), llm.key)
            if (generateLLMAnswer?.body() != null) {
                try {
                    llmAnswer = Json.decodeFromString<ChatBotAnswer>(generateLLMAnswer.body()) // десериализуем
                    //chatHistory.add(MessageData(llmAnswer.message.role, llmAnswer.message.content))
                    result.add(llmAnswer.message.content)
                }
                catch (e: Exception) {
                    println(e.toString())
                    return result
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
    chatBotManager = get(ChatBotManager::class.java)

    val systemPrompt:String = Utils.getDescriptionFromFile("systemPrompt.txt") ?: ""
    chatHistory.add(MessageData("system", systemPrompt))

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
    //val info_mxbai = Mapper.mapperGameData(games)

    //4
    val embeddings = embeddingManager.getNEmbedding(embeddingUrl, info, LLM.NOMIC.key)
    val embeddings_snowflake = embeddingManager.getNEmbedding(embeddingUrl, info_snowflake, LLM.SNOWFLAKE.key)
    //val embeddings_mxbai = embeddingManager.getNEmbedding(embeddingUrl, info_mxbai, LLM.MXBAI.key)
    //val embeddings_snowflake = listOf<InsertData>()
    //val embeddings_mxbai = listOf<InsertData>()

    //5
    val initializer = databaseProvider.database.databaseClient.dbInitializer
    initializer.preloadData(listOf(embeddings).flatten(), LLM.NOMIC.key
    ) { cur, total -> println("Обработано $cur записей из $total...") }
    initializer.preloadData(listOf(embeddings_snowflake).flatten(), LLM.SNOWFLAKE.key
    ) { cur, total -> println("Обработано $cur записей из $total...") }
    //initializer.preloadData(listOf(embeddings_mxbai).flatten(), LLM.MXBAI.key
    //) { cur, total -> println("Обработано $cur записей из $total...") }

    val  text = Utils.getDescriptionFromFile("1.txt")

    //processor.process(text, DistanceMetric.INNER_PRODUCT, LLM.NOMIC.key)
    initHttpServer()
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




