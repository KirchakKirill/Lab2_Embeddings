package org.core.embedding.di

import org.core.embedding.api.EmbeddingManager
import org.core.embedding.impl.EmbeddingManagerImpl
import org.koin.dsl.module


val embeddingModule = module {
    single<EmbeddingManager> {
        EmbeddingManagerImpl()
    }
}