package io.github.orizynpx.arxwipe.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class SwipeInteraction(
    val swipeId: Uuid,
    val paperId: String,
    val type: SwipeType,
    val interactedAt: Instant
)

