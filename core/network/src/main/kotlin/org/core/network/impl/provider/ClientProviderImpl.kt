package org.core.network.impl.provider

import org.core.network.api.ClientProvider
import org.core.network.impl.RemoteClient

internal class ClientProviderImpl: ClientProvider {
    override val client: RemoteClient by lazy {
        RemoteClient()
    }
}