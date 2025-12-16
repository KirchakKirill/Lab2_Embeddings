package org.core.network.impl.adapters

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.core.network.api.NetworkClient

internal class NetworkClientAdapter(
     private val okHttpClient: OkHttpClient
): NetworkClient {

    override fun execute(request: Request): Response {
        return okHttpClient.newCall(request).execute()
    }
}