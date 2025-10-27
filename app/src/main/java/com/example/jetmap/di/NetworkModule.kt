package com.example.jetmap.di

import com.example.jetmap.BuildConfig
import com.example.jetmap.data.network.ApiService
import com.example.jetmap.utils.AppConstants
import com.example.jetmap.utils.ApiConstants
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) logger.level = HttpLoggingInterceptor.Level.BODY
        else logger.level = HttpLoggingInterceptor.Level.BASIC
        return logger
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(AppConstants.Network.CONNECT_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .readTimeout(AppConstants.Network.READ_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .writeTimeout(AppConstants.Network.WRITE_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(ApiConstants.BASE_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}