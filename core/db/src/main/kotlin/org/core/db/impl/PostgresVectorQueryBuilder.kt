package org.core.db.impl

import java.sql.Connection

internal class PostgresVectorQueryBuilder(
    connection: Connection
): DefaultDatabaseQueryBuilder(connection)