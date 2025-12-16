package org.core.db.api

import org.core.dto.DistanceMetric
import org.core.dto.NeighborData

interface DatabaseQueryBuilder{
    suspend fun getNeighbors(emb:List<Float>, distanceMetric: DistanceMetric, model: String): MutableList<NeighborData>?
}