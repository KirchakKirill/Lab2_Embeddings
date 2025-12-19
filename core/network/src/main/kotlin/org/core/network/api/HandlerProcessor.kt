package org.core.network.api

import org.core.dto.DistanceMetric
import org.core.dto.LLM

interface HandlerProcessor {

    suspend fun process(
        text: String?,
        metric: DistanceMetric?,
        model: String,
        llm: LLM?
    ): List<String>
}