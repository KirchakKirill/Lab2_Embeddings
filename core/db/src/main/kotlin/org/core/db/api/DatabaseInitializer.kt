package org.core.db.api

import org.core.dto.InsertData

interface DatabaseInitializer{
    suspend fun preloadData(
        insertData:List<InsertData>,
        model: String,
        onProgress:(Int, Int) -> Unit)
}