package org.core.embedding.di

import org.core.embedding.api.ChatBotManager
import org.core.embedding.api.EmbeddingManager
import org.core.embedding.impl.ChatBotManagerImpl
import org.core.embedding.impl.EmbeddingManagerImpl
import org.koin.dsl.module


val embeddingModule = module {
    single<EmbeddingManager> {
        EmbeddingManagerImpl()
    }
    single<ChatBotManager> {
        ChatBotManagerImpl()
    }
}