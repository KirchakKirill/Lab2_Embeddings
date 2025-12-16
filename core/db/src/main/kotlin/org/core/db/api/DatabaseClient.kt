package org.core.db.api

import javax.sql.DataSource

interface DatabaseClient {
    val dataSource: DataSource
    val dbInitializer: DatabaseInitializer
    val databaseQuery: DatabaseQueryBuilder
}