package io.github.orizynpx.arxwipe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.orizynpx.arxwipe.data.local.converter.DatabaseConverters
import io.github.orizynpx.arxwipe.data.local.dao.ArxwipeDao
import io.github.orizynpx.arxwipe.data.local.entity.*

@Database(
    entities = [
        PaperEntity::class,
        CollectionEntity::class,
        SwipeEntity::class,
        TriageQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class ArxwipeDatabase : RoomDatabase() {
    abstract fun dao(): ArxwipeDao
}