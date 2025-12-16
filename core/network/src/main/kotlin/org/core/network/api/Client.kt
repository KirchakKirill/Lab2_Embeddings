package org.core.network.api

interface Client {
    val networkClient: NetworkClient
    val dataSource: DataSource
}

