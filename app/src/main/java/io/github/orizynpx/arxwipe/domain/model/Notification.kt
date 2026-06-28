package io.github.orizynpx.arxwipe.domain.model

data class Notification(
    val id: String,
    val iconRes: Int,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
