package ru.netology.craftify.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*
import ru.netology.craftify.BuildConfig
import ru.netology.craftify.dto.AuthenticationResponse
import ru.netology.craftify.dto.Token
import ru.netology.craftify.dto.UserResponse
import java.util.concurrent.TimeUnit

private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"

private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .run {
        if (BuildConfig.DEBUG) {
            this.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        } else {
            this
        }
    }
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface AuthApi {

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(@Field("login") login: String, @Field("pass") pass: String): Response<Token>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authUser(
        @Field("login") login: String,
        @Field("password") password: String
    ): Response<AuthenticationResponse>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun userRegist(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("name") name: String,
    ): Response<AuthenticationResponse>

    @FormUrlEncoded
    @Multipart
    @POST("users/registration")
    suspend fun userRegistWithAvatar(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("name") name: String,
        @Field("file") file: MultipartBody.Part,
    ): Response<AuthenticationResponse>

    @GET("users/{user_id}")
    suspend fun getUserById(@Path("user_id") user_id: Long?): Response<UserResponse>

    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>

}

object AuthApiService {
    val service: AuthApi by lazy { retrofit.create() }
}