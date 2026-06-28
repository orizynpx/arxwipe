package io.github.orizynpx.arxwipe.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.orizynpx.arxwipe.data.repository.CollectionRepositoryImpl
import io.github.orizynpx.arxwipe.data.repository.InteractionRepositoryImpl
import io.github.orizynpx.arxwipe.data.repository.NotificationRepositoryImpl
import io.github.orizynpx.arxwipe.data.repository.PaperRepositoryImpl
import io.github.orizynpx.arxwipe.data.repository.SearchHistoryRepositoryImpl
import io.github.orizynpx.arxwipe.data.repository.UserPreferencesRepository
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.NotificationRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import io.github.orizynpx.arxwipe.domain.repository.SearchHistoryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(
        collectionRepositoryImpl: CollectionRepositoryImpl
    ): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindInteractionRepository(
        interactionRepositoryImpl: InteractionRepositoryImpl
    ): InteractionRepository

    @Binds
    @Singleton
    abstract fun bindPaperRepository(
        paperRepositoryImpl: PaperRepositoryImpl
    ): PaperRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        userPreferencesRepository: UserPreferencesRepository
    ): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        searchHistoryRepositoryImpl: SearchHistoryRepositoryImpl
    ): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
}