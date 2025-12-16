package org.core.db.api

interface Database {
    val databaseClient: DatabaseClient
    val databaseMigrator: DatabaseMigrator
}