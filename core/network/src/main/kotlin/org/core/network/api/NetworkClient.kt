package org.core.network.api

import okhttp3.Request
import okhttp3.Response

interface NetworkClient {
    fun execute(request: Request): Response
}