package io.github.orizynpx.arxwipe.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.orizynpx.arxwipe.data.local.ArxwipeDatabase
import io.github.orizynpx.arxwipe.data.local.dao.CollectionDao
import io.github.orizynpx.arxwipe.data.local.dao.InteractionDao
import io.github.orizynpx.arxwipe.data.local.dao.NotificationDao
import io.github.orizynpx.arxwipe.data.local.dao.PaperDao
import io.github.orizynpx.arxwipe.data.local.dao.SearchHistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ArxwipeDatabase {
        return Room.databaseBuilder(
            context,
            ArxwipeDatabase::class.java,
            "arxwipe_database"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun providePaperDao(database: ArxwipeDatabase): PaperDao {
        return database.paperDao()
    }

    @Provides
    fun provideCollectionDao(database: ArxwipeDatabase): CollectionDao {
        return database.collectionDao()
    }

    @Provides
    fun provideInteractionDao(database: ArxwipeDatabase): InteractionDao {
        return database.interactionDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: ArxwipeDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    fun provideNotificationDao(database: ArxwipeDatabase): NotificationDao {
        return database.notificationDao()
    }
}
