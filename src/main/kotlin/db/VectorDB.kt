package org.example.db

import com.pgvector.PGvector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.example.Utils
import org.example.data.InsertData
import org.example.data.LLM
import org.example.data.NeighborData
import java.sql.DriverManager

object VectorDB {

    private val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5433/PGVector",
        System.getenv("DB_USERNAME"),
        System.getenv("DB_PASSWORD")
    )

    suspend fun preloadData(
        insertData:List<InsertData>,
        model: String,
        onProgress:(Int, Int) -> Unit)
    = withContext(Dispatchers.IO) {
        val batchSize = 1000
        val total  = insertData.size
        val table = Utils.tableForModel(model)
        conn.autoCommit = false
        try {
            val statement = conn.prepareStatement("INSERT INTO $table (" +
                    "description, name, metacritic, released, playtime, embedding) VALUES (?,?,?,?,?,?)")
                .use{ stmt ->
                    insertData.chunked(batchSize).forEachIndexed { chunkIndex, chunk ->
                        chunk.forEach {  data ->
                            stmt.setString(1, data.description)
                            stmt.setString(2, data.name)
                            stmt.setInt(3,data.metacritic)
                            stmt.setString(4,data.released)
                            stmt.setInt(5, data.playtime)
                            stmt.setObject(6, PGvector(data.embedding))
                            stmt.addBatch()
                        }
                        stmt.executeBatch()
                        onProgress((chunkIndex + 1) * batchSize, total)
                        yield()
                    }
                }
            conn.commit()
        }
        catch (e: Exception){
            conn.rollback()
            throw  e
        }
        finally {
            conn.autoCommit = true
        }
    }

    suspend fun getNeighbors(emb:List<Float>, distanceMetric: DistanceMetric, model: String): MutableList<NeighborData>? = withContext(Dispatchers.IO) {
            var query:String = ""
            val tableName = Utils.tableForModel(model)
            if (tableName != null) {
                when {
                    distanceMetric == DistanceMetric.L2 -> {
                        query = "SELECT * FROM $tableName ORDER BY embedding <-> ? LIMIT 5"
                    }
                    distanceMetric == DistanceMetric.COSINE -> {
                        query = "SELECT * FROM $tableName ORDER BY embedding <=> ? LIMIT 5"
                    }
                    distanceMetric == DistanceMetric.INNER_PRODUCT -> {
                        query = "SELECT * FROM $tableName ORDER BY embedding <#> ? LIMIT 5"
                    }
                }
                try {
                    val stmt = conn.prepareStatement(query)
                        .use {
                                statement ->
                            statement.setObject(1, PGvector(emb))
                            val rs = statement.executeQuery()
                            val neighborsInfo = mutableListOf<NeighborData>()
                            while (rs.next()){
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
                }
                catch (e: Exception){
                    println("Error: ${e.message}")
                    return@withContext null
                }
            }
        return@withContext  null
    }
}

enum class DistanceMetric {
    L2, COSINE, INNER_PRODUCT
}