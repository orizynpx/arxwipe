package io.github.orizynpx.arxwipe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.orizynpx.arxwipe.data.local.converter.RoomConverters
import io.github.orizynpx.arxwipe.data.local.dao.CollectionDao
import io.github.orizynpx.arxwipe.data.local.dao.InteractionDao
import io.github.orizynpx.arxwipe.data.local.dao.NotificationDao
import io.github.orizynpx.arxwipe.data.local.dao.PaperDao
import io.github.orizynpx.arxwipe.data.local.dao.SearchHistoryDao
import io.github.orizynpx.arxwipe.data.local.entity.AuthorEntity
import io.github.orizynpx.arxwipe.data.local.entity.CollectionEntity
import io.github.orizynpx.arxwipe.data.local.entity.CollectionPaperCrossRef
import io.github.orizynpx.arxwipe.data.local.entity.NotificationEntity
import io.github.orizynpx.arxwipe.data.local.entity.PaperAuthorCrossRef
import io.github.orizynpx.arxwipe.data.local.entity.PaperEntity
import io.github.orizynpx.arxwipe.data.local.entity.SearchHistoryEntity
import io.github.orizynpx.arxwipe.data.local.entity.SwipeInteractionEntity
import io.github.orizynpx.arxwipe.data.local.entity.TriagePaperCrossRef

@Database(
    entities = [
        PaperEntity::class,
        AuthorEntity::class,
        PaperAuthorCrossRef::class,
        CollectionEntity::class,
        CollectionPaperCrossRef::class,
        TriagePaperCrossRef::class,
        SwipeInteractionEntity::class,
        SearchHistoryEntity::class,
        NotificationEntity::class,
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class ArxwipeDatabase : RoomDatabase() {
    abstract fun paperDao(): PaperDao
    abstract fun collectionDao(): CollectionDao
    abstract fun interactionDao(): InteractionDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun notificationDao(): NotificationDao
}
