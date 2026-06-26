package io.github.orizynpx.arxwipe.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.orizynpx.arxwipe.data.repository.CollectionRepositoryImpl
import io.github.orizynpx.arxwipe.data.repository.InteractionRepositoryImpl
import io.github.orizynpx.arxwipe.data.repository.PaperRepositoryImpl
import io.github.orizynpx.arxwipe.domain.repository.CollectionRepository
import io.github.orizynpx.arxwipe.domain.repository.InteractionRepository
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPaperRepository(impl: PaperRepositoryImpl): PaperRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindInteractionRepository(impl: InteractionRepositoryImpl): InteractionRepository
}