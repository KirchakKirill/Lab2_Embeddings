package org.core.db.impl

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.core.db.api.DatabaseMigrator
import java.sql.Connection
import java.sql.DriverManager
import kotlin.system.exitProcess

internal class LiquibaseMigrator(
    private val connection: Connection
): DatabaseMigrator {

    override fun migrate(): Boolean {
        connection.use {
            return  migrate(it)
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

            println("org.core.db.api.Database migration completed successfully!")
            return true

        } catch (e: Exception) {
            println("Migration failed: ${e.message}")
            e.printStackTrace()
            exitProcess(1)
        }
    }
}