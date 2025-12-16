package org.core.db.impl

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.core.db.api.DatabaseClient
import org.core.db.api.DatabaseInitializer
import org.core.db.api.DatabaseQueryBuilder
import javax.sql.DataSource

internal class PostgresVectorClient: DatabaseClient {

    override val dataSource: DataSource by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5433/PGVector"
            username = System.getenv("DB_USERNAME")
            password = System.getenv("DB_PASSWORD")
            maximumPoolSize = 10
        }
         HikariDataSource(config)
    }

    override val databaseQuery: DatabaseQueryBuilder by lazy {
        PostgresVectorQueryBuilder(dataSource.connection)
    }

    override val dbInitializer: DatabaseInitializer by lazy {
        PostgresVectorInitializer(dataSource.connection)
    }
}