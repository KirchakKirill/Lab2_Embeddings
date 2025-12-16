package org.core.db.impl

import org.core.db.api.Database
import org.core.db.api.DatabaseProvider

internal class PostgresVectorProvider: DatabaseProvider {
    override val database: Database by lazy {
        PostgresVector()
    }
}