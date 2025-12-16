package org.core.db.impl

import com.pgvector.PGvector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.core.db.api.DatabaseInitializer
import org.core.dto.InsertData
import org.core.utils.Utils
import java.sql.Connection
import kotlin.use

internal abstract class DefaultDatabaseInitializer(
    private val connection: Connection
): DatabaseInitializer {

    override suspend fun preloadData(
        insertData:List<InsertData>,
        model: String,
        onProgress:(Int, Int) -> Unit)
            = withContext(Dispatchers.IO) {
        val batchSize = 1000
        val total = insertData.size
        val table = Utils.tableForModel(model)
        connection.autoCommit = false
        try {
            val statement = connection.prepareStatement(
                "INSERT INTO $table (" +
                        "description, name, metacritic, released, playtime, embedding) VALUES (?,?,?,?,?,?)"
            )
                .use { stmt ->
                    insertData.chunked(batchSize).forEachIndexed { chunkIndex, chunk ->
                        chunk.forEach { data ->
                            stmt.setString(1, data.description)
                            stmt.setString(2, data.name)
                            stmt.setInt(3, data.metacritic)
                            stmt.setString(4, data.released)
                            stmt.setInt(5, data.playtime)
                            stmt.setObject(6, PGvector(data.embedding))
                            stmt.addBatch()
                        }
                        stmt.executeBatch()
                        onProgress((chunkIndex + 1) * batchSize, total)
                        yield()
                    }
                }
            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = true
        }
    }
}