package org.core.mapper

import org.core.dto.GameData
import org.core.dto.GameInfo

object Mapper {

    fun mapperGameData(games:List<GameInfo?>):List<GameData> {
        return games.mapNotNull { game ->
            with(game) {
                takeIf { listOf(
                    this?.name,
                    this?.description,
                    this?.released,
                    this?.playtime,
                    this?.metacritic
                ).all { it != null } }?.let { g->
                    GameData(
                        name = g.name!!,
                        description = g.description!!,
                        playtime = g.playtime!!,
                        released = g.released!!,
                        metacritic = g.metacritic!!
                    )
                }
            }
        }
    }
}