package ru.netology.craftify.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.craftify.dto.EventRequest
import ru.netology.craftify.dto.EventResponse

interface EventApiService {
    @GET("events")
    suspend fun getEvents(): Response<List<EventResponse>>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<EventResponse>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Long): Response<EventResponse>

    @POST("events/{id}/participants")
    suspend fun partEventById(@Path("id") id: Long): Response<EventResponse>

    @DELETE("events/{id}/participants")
    suspend fun nonPartEventById(@Path("id") id: Long): Response<EventResponse>

    @POST("events")
    suspend fun saveEvent(@Body eventRequest: EventRequest): Response<EventResponse>
}