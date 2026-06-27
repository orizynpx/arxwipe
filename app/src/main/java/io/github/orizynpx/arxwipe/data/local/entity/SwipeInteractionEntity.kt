package io.github.orizynpx.arxwipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "swipe_interactions")
data class SwipeInteractionEntity(
    @PrimaryKey
    val swipeId: String,
    val paperId: String,
    val type: String, 
    val interactedAt: Instant
)
