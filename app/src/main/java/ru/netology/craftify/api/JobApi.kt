package ru.netology.craftify.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.craftify.dto.JobRequest
import ru.netology.craftify.dto.JobResponse

interface JobApiService {
    @POST("my/jobs")
    suspend fun saveJob(@Body jobRequest: JobRequest): Response<JobResponse>

    @DELETE("my/jobs/{job_id}")
    suspend fun removeJobById(@Path("job_id") job_id: Long): Response<Unit>

    @GET("{user_id}/jobs")
    suspend fun getJobsByUserId(@Path("user_id") user_id: Long): Response<List<JobResponse>>
}