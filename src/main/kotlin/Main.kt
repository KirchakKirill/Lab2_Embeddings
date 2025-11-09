package org.example

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.*
import org.example.data.RequestData
import org.example.data.ResultEmbedding
import org.example.db.DistanceMetric
import org.example.db.VectorDB
import org.example.manager.GameManager
import org.example.manager.NomicEmbeddingManager

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(): kotlin.Unit = runBlocking {

    val nomicEmbeddingManager = createTable()


    val text = Utils.getDescriptionFromFile("1.txt") // здесь мы считываем пользовательское описание,
    // которое будет сравниваться на похожесть с уже сохраненными
    println(text)
    text?.let {
        val emb = nomicEmbeddingManager.getEmbeddingOneGame( // получаем embedding пользовательского описания
            RequestData(
                model = "nomic-embed-text:v1.5",
                prompt = text
            )
        )
        if (emb?.body() != null ){
            val resEmb = Json.decodeFromString<ResultEmbedding>(emb.body()) // десериализуем
            val res =  VectorDB.getNeighbors(resEmb.embedding, DistanceMetric.INNER_PRODUCT) // получаем пять ближайших соседей
                // DistanceMetric - функции расстояния
            res?.forEachIndexed { index, item -> println("$index: $item")}
        }
    }

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
    val info = manager.generateRequestData(games)


    val nomicEmbeddingManager  = NomicEmbeddingManager("http://localhost:11434/api/embeddings")
    val embeddings = nomicEmbeddingManager.getNEmbedding(info)


    VectorDB.createTableEmbedding()
    VectorDB.preloadData(embeddings)
    return nomicEmbeddingManager
}


