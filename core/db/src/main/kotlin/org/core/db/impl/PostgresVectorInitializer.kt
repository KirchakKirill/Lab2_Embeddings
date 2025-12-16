package org.core.db.impl

import java.sql.Connection

internal class PostgresVectorInitializer(
    connection: Connection
): DefaultDatabaseInitializer(connection)