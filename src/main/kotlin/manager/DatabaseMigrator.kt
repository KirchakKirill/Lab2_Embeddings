package org.example.manager

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.Connection
import java.sql.DriverManager
import kotlin.system.exitProcess

object DatabaseMigrator {

    @Volatile var isMigrated  = false
        private set

    fun migrate() {
        val url = "jdbc:postgresql://localhost:5433/PGVector"
        val username = System.getenv("DB_USERNAME")
        val password = System.getenv("DB_PASSWORD")

        DriverManager.getConnection(url, username, password).use { connection ->
            isMigrated = migrate(connection)
        }
    }

    fun migrate(connection: Connection): Boolean {
        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(connection))

        val liquibase = Liquibase(
            "db/changelog/db.changelog-master.yaml",
            ClassLoaderResourceAccessor(),
            database
        )

        try {
            println("Starting database migration...")

            val unrunChanges = liquibase.listUnrunChangeSets(Contexts(), LabelExpression())
            println("Unrun changes: ${unrunChanges.size}")

            liquibase.update(Contexts(), LabelExpression())

            println("Database migration completed successfully!")
            return true

        } catch (e: Exception) {
            println("Migration failed: ${e.message}")
            e.printStackTrace()
            exitProcess(1)
        }
    }
}