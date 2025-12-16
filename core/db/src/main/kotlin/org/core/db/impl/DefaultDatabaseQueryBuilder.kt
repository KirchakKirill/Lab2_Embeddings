package org.core.db.impl

import com.pgvector.PGvector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.core.dto.DistanceMetric
import org.core.db.api.DatabaseQueryBuilder
import org.core.dto.NeighborData
import org.core.utils.Utils
import java.sql.Connection
import kotlin.use

internal abstract class DefaultDatabaseQueryBuilder(
    private val connection: Connection
): DatabaseQueryBuilder {

    override suspend fun getNeighbors(emb:List<Float>, distanceMetric: DistanceMetric, model: String): MutableList<NeighborData>? =
        withContext(Dispatchers.IO) {
            val tableName = Utils.tableForModel(model)
            if (tableName != null) {
                val query = when (distanceMetric) {
                    DistanceMetric.L2 -> {
                        "SELECT * FROM $tableName ORDER BY embedding <-> ? LIMIT 5"
                    }

                    DistanceMetric.COSINE -> {
                        "SELECT * FROM $tableName ORDER BY embedding <=> ? LIMIT 5"
                    }

                    DistanceMetric.INNER_PRODUCT -> {
                        "SELECT * FROM $tableName ORDER BY embedding <#> ? LIMIT 5"
                    }
                }
                try {
                    val stmt = connection.prepareStatement(query)
                        .use { statement ->
                            statement.setObject(1, PGvector(emb))
                            val rs = statement.executeQuery()
                            val neighborsInfo = mutableListOf<NeighborData>()
                            while (rs.next()) {
                                val data = NeighborData(
                                    name = rs.getString("name"),
                                    description = rs.getString("description"),
                                    playtime = rs.getInt("playtime"),
                                    metacritic = rs.getInt("metacritic"),
                                    released = rs.getString("released")
                                )
                                neighborsInfo.add(data)
                            }
                            neighborsInfo
                        }
                    return@withContext stmt
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                    return@withContext null
                }
            }
            return@withContext null
        }
}