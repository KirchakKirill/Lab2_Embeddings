package org.core.network.api

import org.core.dto.DistanceMetric

interface HandlerProcessor {

    suspend fun process(text: String?,
                metric: DistanceMetric?,
                model: String): List<String>
}