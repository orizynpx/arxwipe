package io.github.orizynpx.arxwipe.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.orizynpx.arxwipe.MainActivity
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.domain.model.Notification
import io.github.orizynpx.arxwipe.domain.repository.NotificationRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.usecase.CompileNewTriageUseCase
import timber.log.Timber
import java.util.UUID

@HiltWorker
class TriageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val compileNewTriageUseCase: CompileNewTriageUseCase,
    private val notificationRepository: NotificationRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val force = inputData.getBoolean(KEY_FORCE, defaultValue = false)
            Timber.d("Memulai background sync untuk triage (force=$force)")
            val triage = compileNewTriageUseCase(force)

            if (triage.papers.isNotEmpty()) {
                val message = "Your new batch of ${triage.papers.size} papers is ready!"
                saveNotification(message)
                showNotification(message)
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Eror ketika mencoba sync triage")
            if (e.message?.contains("No new papers") == true) {
                Result.failure(workDataOf("error" to e.message))
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        const val KEY_FORCE = "force"
    }

    private suspend fun saveNotification(message: String) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            iconRes = R.drawable.arxwipe_logomark,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepository.insertNotification(notification)
    }

    private fun showNotification(message: String) {
        val channelId = "triage_sync_notifications"
        val notificationId = 101

        val name = "Triage Sync"
        val descriptionText = "Notifications for daily paper triage updates"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.arxwipe_logomark)
            .setContentTitle("ArXwipe")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                Timber.e(e, "Notifikasi belum diizinkan")
            }
        }
    }
}
