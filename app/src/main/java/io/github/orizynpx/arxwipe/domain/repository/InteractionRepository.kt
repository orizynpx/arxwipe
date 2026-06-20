package io.github.orizynpx.arxwipe.domain.repository

import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.domain.model.Triage

interface InteractionRepository {
    suspend fun recordSwipe(paperId: String, type: SwipeType)
    suspend fun getActiveTriage(): Triage?
    suspend fun saveTriage(triage: Triage)
}