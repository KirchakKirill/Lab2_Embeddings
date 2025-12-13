package org.example

import org.example.data.GameData
import org.example.data.RequestData

fun GameData.mapperToRequestData(model: String): RequestData {
        return RequestData(
            model = model,
            prompt = "Название игры: ${this.name}.\n" +
                    "Дата релиза: ${this.released}.\n" +
                    "Рейтинг на Metacritic: ${this.metacritic}.\n" +
                    "Среднее время прохождения: ${this.playtime} часов.\n" +
                    "Описание игры: ${this.description}."
        )
}
