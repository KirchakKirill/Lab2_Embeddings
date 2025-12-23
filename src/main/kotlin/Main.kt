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
import java.io.File
import java.net.http.HttpResponse

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.


lateinit var embeddingManager: EmbeddingManager
lateinit var clientProvider: ClientProvider
lateinit var databaseProvider: DatabaseProvider
lateinit var chatBotManager: ChatBotManager

const val embeddingUrl =  "http://localhost:11434/api/embeddings"
const val generateChatMessageUrl =  "http://localhost:11434/api/chat"
const val searchInternetUrl =  "https://ollama.com/api/web_search"

val chatHistory: MutableList<MessageData> = mutableListOf()
var toolsLLM: MutableList<ToolData> = mutableListOf()

val processor = object : HandlerProcessor {

    override suspend fun process(
        text: String?,
        metric: DistanceMetric?,
        model: String,
        llm: LLM?
    ): List<String> {
        val result: MutableList<String> = mutableListOf()
        text?.let {
            chatHistory.add(MessageData("user", text + " &&&EMBMODE<$model> &&&METRICKMODE:<${metric!!.name}>"))
            Utils.addRecordToFile("chatHistoryLogs.txt", "<user>: $text" + "\n&&&EMBMODE<$model> &&&METRICKMODE:<${metric!!.name}> \n\n")

            var llmAnswer: ChatBotAnswer? = null

            llmAnswer = llmRequest(result, llm!!)
            if (llmAnswer==null) {
                return result
            }

            if (llmAnswer!!.message.tool_calls.isEmpty()) {
                return result
            }

            var webSearchResult: HttpResponse<String>? = null
            var databaseSearchResult: String? = null

            val toolsHistory: MutableList<MessageData> = mutableListOf()

            llmAnswer.message.tool_calls.map { tool ->
                when (tool.function.name) {
                    "searchInternet" -> webSearchResult = chatBotManager.searchInternet(tool.function.arguments.url, tool.function.arguments.query!!, tool.function.arguments.max_results!!)
                    "searchDatabase" -> databaseSearchResult = databaseSearch(tool.function.arguments.url, tool.function.arguments.model!!, tool.function.arguments.metric!!, tool.function.arguments.prompt!!)
                    else -> print("No tool specified")
                }
            }

            toolsHistory.add(MessageData("tool", tool_name = "searchInternet", content = webSearchResult!!.body().takeIf { !it.isNullOrEmpty() } ?: "Не удалось получить данные из интернета"))
            toolsHistory.add(MessageData("tool", tool_name = "searchDatabase", content = databaseSearchResult.takeIf { !it.isNullOrEmpty() } ?: "Не удалось получить данные из базы данных"))
            chatHistory += toolsHistory
            Utils.addRecordToFile("chatHistoryLogs.txt", "<tool>: ${webSearchResult!!.body()} \n")
            Utils.addRecordToFile("chatHistoryLogs.txt","<tool>: ${databaseSearchResult.takeIf { !it.isNullOrEmpty() } ?: "Не удалось получить данные из базы данных"} \n")

            llmAnswer = llmRequest(result, llm)
            if (llmAnswer==null) { return result }
        }
        return result
    }

    suspend fun databaseSearch(embeddingUrl: String, model: String, metric: String, prompt: String): String {
        var summaryDescriptionForLLM: String = ""
        var distanceMetric: DistanceMetric? = null

        when (metric) {
            "L2" -> distanceMetric= DistanceMetric.L2
            "COSINE" -> distanceMetric= DistanceMetric.COSINE
            "INNER_PRODUCT" -> distanceMetric= DistanceMetric.INNER_PRODUCT
        }

        val emb = embeddingManager.getOneEmbedding(
            embeddingUrl,
            UserEmbeddingRequest(
                RequestData(
                    model = model,
                    prompt = prompt
                )
            )
        )

        if (emb?.body() != null) {
            val resEmb = Json.decodeFromString<ResultEmbedding>(emb.body()) // десериализуем
            val res = databaseProvider.database.databaseClient.databaseQuery.getNeighbors(
                resEmb.embedding,
                distanceMetric!!,
                model
            ) // получаем пять ближайших соседей
            // DistanceMetric - функции расстояния
            res?.forEachIndexed { index, item ->
                println(
                    "$index: name: ${item.name}\n" +
                            "playtime: ${item.playtime}\n" +
                            "released: ${item.released}\n" +
                            "metacritic: ${item.metacritic}\n" +
                            "description: ${item.description}\n"
                )
                summaryDescriptionForLLM +=
                    "\n---------------game-----------------\n" +
                            "Name: " + item.name +
                            "Playtime: " + item.playtime.toString() +
                            "Released: " + item.released +
                            "Metacritic: " + item.metacritic.toString() +
                            "Description: " + item.description
                //result.add(item.orEmpty().replace("\"", "").replace("'", ""))
            }
        }

        return summaryDescriptionForLLM
    }

    suspend fun llmRequest(result: MutableList<String>, llm: LLM): ChatBotAnswer? {
        var generateLLMAnswer = chatBotManager.generateChatMessage(generateChatMessageUrl, chatHistory, llm!!.key, toolsLLM)
        var llmAnswer: ChatBotAnswer? = null

        if (generateLLMAnswer?.body() != null) {
            try {
                llmAnswer = Json.decodeFromString<ChatBotAnswer>(generateLLMAnswer!!.body()) // десериализуем
                chatHistory.add(MessageData(llmAnswer!!.message.role, llmAnswer!!.message.content, llmAnswer!!.message.tool_calls))
                Utils.addRecordToFile("chatHistoryLogs.txt", "<${llmAnswer!!.message.role}>: ${llmAnswer!!.message.content} \n |||tool_calls|||: ${llmAnswer!!.message.tool_calls} \n\n\n")
                if (llmAnswer!!.message.tool_calls.isEmpty()) {
                    result.add(llmAnswer!!.message.content)
                }
                return llmAnswer
            }
            catch (e: Exception) {
                println(e.toString())
                return null
            }
        }
        return null
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

    var systemPrompt:String = Utils.getDescriptionFromFile("systemPrompt.txt") ?: ""
    systemPrompt = systemPrompt.replace("&&&API_INTERNET", "$searchInternetUrl").replace("&&&API_EMBEDDING", "$embeddingUrl")
    val webSearchToolJSON: String = Utils.getDescriptionFromFile("src/main/resources/Tools LLM/WebSearchTool.JSON") ?: ""
    val databaseSearchToolJSON: String = Utils.getDescriptionFromFile("src/main/resources/Tools LLM/DatabaseSearchTool.JSON") ?: ""

    toolsLLM.add(Json.decodeFromString<ToolData>(webSearchToolJSON)) //добавляем инструмент веб-поиска в список инструментов
    toolsLLM.add(Json.decodeFromString<ToolData>(databaseSearchToolJSON)) //добавляем инструмент поиска в базе данных
    chatHistory.add(MessageData("system", systemPrompt)) //добавялем системынй промпт

    val logs = File("chatHistoryLogs.txt")
    logs.writeText("")//чистим логи при перезапуске
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




