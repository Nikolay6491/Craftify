package ru.netology.craftify.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.craftify.BuildConfig
import ru.netology.craftify.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
    }

    @Singleton
    @Provides
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Singleton
    @Provides
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request = appAuth.authStateFlow.value.token?.let {
                chain.request()
                    .newBuilder()
                    .addHeader("Authorization", it)
                    .build()
            } ?: chain.request()

            chain.proceed(request)
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        okhttp: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(okhttp)
        .baseUrl(BASE_URL)
        .build()

    @Singleton
    @Provides
    fun providePostsApiService(
        retrofit: Retrofit
    ): PostsApiService = retrofit.create()

    @Singleton
    @Provides
    fun provideAuthApi(
        retrofit: Retrofit
    ): AuthApi = retrofit.create()

    @Singleton
    @Provides
    fun provideEventApiService(
        retrofit: Retrofit
    ): EventApiService = retrofit.create()

    @Singleton
    @Provides
    fun provideJobApiService(
        retrofit: Retrofit
    ): JobApiService = retrofit.create()
}