package org.core.network.factory

import com.sun.net.httpserver.HttpHandler
import org.core.network.api.HandlerProcessor
import org.core.network.impl.handler.HttpRestHandler

class HttpHandlerFactory {

    class Builder {
        private var processor: HandlerProcessor? = null

        fun withProcessor(processor: HandlerProcessor) = apply {
            this.processor = processor
        }

        fun build(): HttpHandler {
            return HttpRestHandler(
                processor ?: throw IllegalStateException("Processor must be set")
            )
        }
    }
}