package org.core.network.di

import org.core.network.api.ClientProvider
import org.core.network.impl.provider.ClientProviderImpl
import org.koin.dsl.module


val networkModule = module {
    single<ClientProvider>{
        ClientProviderImpl()
    }
}