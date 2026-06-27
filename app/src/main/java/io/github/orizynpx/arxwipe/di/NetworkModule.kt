package io.github.orizynpx.arxwipe.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.orizynpx.arxwipe.data.remote.ArxivApiService
import io.github.orizynpx.arxwipe.data.remote.RateLimitInterceptor
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @OptIn(ExperimentalXmlUtilApi::class)
    @Provides
    @Singleton
    fun provideXmlParser(): XML {
        return XML {
            autoPolymorphic = true
            repairNamespaces = true
            policy = DefaultXmlSerializationPolicy {
                ignoreUnknownChildren()
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(RateLimitInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://export.arxiv.org/api/")
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideArxivApiService(retrofit: Retrofit): ArxivApiService {
        return retrofit.create(ArxivApiService::class.java)
    }
}
