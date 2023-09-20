package ru.netology.craftify.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.craftify.dto.*

interface PostsApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<PostResponse>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<PostResponse>

    @POST("posts")
    suspend fun save(@Body postRequest: PostRequest): Response<PostResponse>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part file: MultipartBody.Part): Response<MediaResponse>

    @GET("{author_id}/wall")
    suspend fun getPostsByAuthor(@Path("user_id") user_id: Long): Response<List<PostResponse>>
}