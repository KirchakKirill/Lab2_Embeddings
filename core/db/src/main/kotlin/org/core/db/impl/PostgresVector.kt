package org.core.db.impl

import org.core.db.api.Database
import org.core.db.api.DatabaseClient
import org.core.db.api.DatabaseMigrator

internal class PostgresVector: Database {

    override val databaseClient: DatabaseClient by lazy {
        PostgresVectorClient()
    }

    override val databaseMigrator: DatabaseMigrator by lazy {
        LiquibaseMigrator(databaseClient.dataSource.connection)
    }
}