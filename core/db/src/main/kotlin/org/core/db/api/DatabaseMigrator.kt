package org.core.db.api

interface DatabaseMigrator {
    fun migrate(): Boolean
}