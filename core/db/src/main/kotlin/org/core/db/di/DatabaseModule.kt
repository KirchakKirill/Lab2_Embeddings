package org.core.db.di

import org.core.db.api.DatabaseProvider
import org.core.db.impl.PostgresVectorProvider
import org.koin.dsl.module


val databaseModule = module {
    single<DatabaseProvider> {
        PostgresVectorProvider()
    }
}
