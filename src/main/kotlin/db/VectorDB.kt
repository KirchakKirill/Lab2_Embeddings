package org.example.db

import com.pgvector.PGvector
import org.example.data.InsertData
import java.sql.DriverManager

object VectorDB {

    private val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5433/PGVector",
        System.getenv("DB_USERNAME"),
        System.getenv("DB_PASSWORD")
    )


    fun createTableEmbedding(){
        val setupStatement = conn.createStatement()
        setupStatement.executeUpdate("CREATE EXTENSION IF NOT EXISTS VECTOR")
        setupStatement.executeUpdate("DROP TABLE IF EXISTS MXGAMES")
        setupStatement.executeUpdate("DROP TABLE IF EXISTS MXGAMESSNOWFLAKE")
        setupStatement.executeUpdate("DROP TABLE IF EXISTS MXGAMESMXBAI")

        PGvector.addVectorType(conn)

        val createStmt = conn.createStatement()
        createStmt.executeUpdate("CREATE TABLE MXGAMES (id bigserial PRIMARY KEY, description TEXT NOT NULL,embedding vector(768))")
        createStmt.executeUpdate("CREATE TABLE MXGAMESSNOWFLAKE (id bigserial PRIMARY KEY, description TEXT NOT NULL,embedding vector(1024))")
        createStmt.executeUpdate("CREATE TABLE MXGAMESMXBAI (id bigserial PRIMARY KEY, description TEXT NOT NULL,embedding vector(1024))")
    }

    fun preloadData(insertData:List<InsertData>, tableName: String){
        conn.autoCommit = false
        try {
            val statement = conn.prepareStatement("INSERT INTO $tableName (description,embedding) VALUES (?,?)")
                .use{ stmt ->
                    insertData.forEach { data ->
                        stmt.setString(1, data.description)
                        stmt.setObject(2, PGvector(data.embedding))
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
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

    fun getNeighbors(emb:List<Float>, distanceMetric: DistanceMetric, model_type: String): MutableList<String>?{
        var tableName = ""
        when {
            model_type == "nomic-embed-text:v1.5" -> {
                tableName = "MXGAMES"
            }
            model_type == "snowflake-arctic-embed2" -> {
                tableName = "MXGAMESSNOWFLAKE"
            }
            model_type == "mxbai-embed-large" -> {
                tableName = "MXGAMESMXBAI"
            }
        }
        try {
            var query:String = ""
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
            val stmt = conn.prepareStatement(query)
                .use {
                        statement ->
                    statement.setObject(1, PGvector(emb))
                    val rs = statement.executeQuery()
                    val neighborsDescription = mutableListOf<String>()
                    while (rs.next()){
                        neighborsDescription.add(rs.getString("description"))
                    }
                    neighborsDescription
                }
            return stmt
        }
        catch (e: Exception){
         println("Error: ${e.message}")
            return null
        }

    }

}

enum class DistanceMetric {
    L2, COSINE, INNER_PRODUCT
}