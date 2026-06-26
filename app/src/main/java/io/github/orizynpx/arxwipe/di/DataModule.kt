package io.github.orizynpx.arxwipe.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.orizynpx.arxwipe.data.local.ArxwipeDatabase
import io.github.orizynpx.arxwipe.data.local.dao.ArxwipeDao
import io.github.orizynpx.arxwipe.data.remote.ArxivApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ArxwipeDatabase {
        return Room.databaseBuilder(
            context,
            ArxwipeDatabase::class.java,
            "arxwipe_clean_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideDao(db: ArxwipeDatabase): ArxwipeDao = db.dao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://export.arxiv.org/api/")
            .client(OkHttpClient())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ArxivApiService {
        return retrofit.create(ArxivApiService::class.java)
    }
}