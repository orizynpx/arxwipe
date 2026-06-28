package io.github.orizynpx.arxwipe.data.repository

import io.github.orizynpx.arxwipe.data.local.dao.NotificationDao
import io.github.orizynpx.arxwipe.data.local.entity.NotificationEntity
import io.github.orizynpx.arxwipe.domain.model.Notification
import io.github.orizynpx.arxwipe.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getAllNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertNotification(notification: Notification) {
        notificationDao.insertNotification(notification.toEntity())
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun clearAllNotifications() {
        notificationDao.clearAllNotifications()
    }

    private fun NotificationEntity.toDomain(): Notification {
        return Notification(
            id = id,
            iconRes = iconRes,
            message = message,
            timestamp = timestamp,
            isRead = isRead
        )
    }

    private fun Notification.toEntity(): NotificationEntity {
        return NotificationEntity(
            id = id,
            iconRes = iconRes,
            message = message,
            timestamp = timestamp,
            isRead = isRead
        )
    }
}
