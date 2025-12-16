// di/AppModule.kt
package org.example

import org.core.db.di.databaseModule
import org.core.embedding.di.embeddingModule
import org.core.network.di.networkModule
import org.koin.dsl.module

val appModule = module {
    includes(databaseModule, networkModule, embeddingModule)
}