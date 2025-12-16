package org.core.network.impl

import okhttp3.OkHttpClient
import org.core.network.api.Client
import org.core.network.impl.adapters.NetworkClientAdapter
import org.core.network.impl.handler.HttpRestHandler

internal class RemoteClient: Client {
    override val networkClient: NetworkClientAdapter by lazy {
        NetworkClientAdapter(
            OkHttpClient.Builder().addInterceptor {
                    chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter("key", System.getenv("key"))
                    .build()

                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .build()

                chain.proceed(newRequest)
            }.build()
        )
    }

    override val dataSource: GameApi by lazy {
        GameApi(networkClient)
    }
}