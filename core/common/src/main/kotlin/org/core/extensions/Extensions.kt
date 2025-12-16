package org.core.extensions

import org.core.dto.GameData
import org.core.dto.RequestData
import java.net.URI

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

fun String.toUri(): URI {
    return URI.create(this)
}
