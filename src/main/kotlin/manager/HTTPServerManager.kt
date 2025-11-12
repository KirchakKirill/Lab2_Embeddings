package org.example.manager

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.example.data.HTTPResuestData
import org.example.data.ResponseData
import org.example.db.DistanceMetric
import org.example.processText
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class HttpRestHandler(val nomicEmbeddingManager: NomicEmbeddingManager) : HttpHandler {
    @Throws(IOException::class)
    override fun handle(t: HttpExchange) {
        // Добавляем CORS заголовки
        with(t.responseHeaders) {
            add("Access-Control-Allow-Origin", "*") // Разрешаем запросы со всех источников
            add("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
            add("Access-Control-Allow-Headers", "Content-Type")
        }

        // Обрабатываем preflight-запрос (OPTIONS)
        if (t.requestMethod == "OPTIONS") {
            t.sendResponseHeaders(204, -1) // No Content
            t.close()
            return
        }

        // Обрабатываем только POST (или можно и другие)
        if (t.requestMethod == "POST") {
            // Читаем тело запроса
            val isr = InputStreamReader(t.requestBody, Charsets.UTF_8)
            val br = BufferedReader(isr)
            val requestBody = StringBuilder()
            var line: String?
            while ((br.readLine().also { line = it }) != null) {
                requestBody.append(line)
            }
            br.close()
            val requestText = requestBody.toString()
            val requestObj = Json.decodeFromString<HTTPResuestData>(requestText) // десериализуем

            println("Received POST url: ${t.requestURI}, body: $requestText")
            var response = emptyList<String>()

            var currentTypeMetric: DistanceMetric? = null;
            when (requestObj.metric) {
                "L2" -> currentTypeMetric = DistanceMetric.L2
                "COSINE" -> currentTypeMetric = DistanceMetric.COSINE
                "INNER_PRODUCT" -> currentTypeMetric = DistanceMetric.INNER_PRODUCT
            }

            var currentModel: String = "nomic-embed-text:v1.5";
            when (requestObj.model) {
                "nomic-model" -> currentModel = "nomic-embed-text:v1.5";
                "snowflake-model" -> currentModel = "snowflake-arctic-embed2";
                "mxbai-model" -> currentModel = "mxbai-embed-large";
            }

            runBlocking {
                response = processText(requestObj.desc, currentTypeMetric, currentModel, nomicEmbeddingManager)
            }
            //response = "OK"
            val responseData = ResponseData(response)
            val responseObj = Json.Default.encodeToString(responseData)
            println("responseObj: $responseObj")

            // Формируем ответ
            //val responseJson = "{\"desc\":\"$response\"}"
            t.sendResponseHeaders(200, responseObj.toByteArray().size.toLong())
            val os = t.responseBody
            os.write(responseObj.toByteArray())
            os.close()
        } else {
            // Если метод не POST
            t.sendResponseHeaders(405, -1) // Method Not Allowed
            t.close()
        }
    }
}