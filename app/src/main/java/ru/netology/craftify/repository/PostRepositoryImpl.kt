package ru.netology.craftify.repository

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.craftify.api.*
import ru.netology.craftify.auth.AuthState
import ru.netology.craftify.dao.EventDao
import ru.netology.craftify.dao.JobDao
import ru.netology.craftify.dao.PostDao
import ru.netology.craftify.dao.UserDao
import ru.netology.craftify.dto.*
import ru.netology.craftify.entity.*
import ru.netology.craftify.type.AttachmentType
import ru.netology.craftify.error.ApiError
import ru.netology.craftify.error.AppError
import ru.netology.craftify.error.NetworkError
import ru.netology.craftify.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val eventDao: EventDao,
    private val userDao: UserDao,
    private val jobDao: JobDao,
    private val postsApiService: PostsApiService,
    private val eventApiService: EventApiService,
    private val authApi: AuthApi,
    private val jobApiService: JobApiService,
) : PostRepository {
    override val posts = postDao.getPosts()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)
    override val events = eventDao.getEvents()
        .map(List<EventEntity>::toDto)
        .flowOn(Dispatchers.Default)
    override val users = userDao.getUsers()
        .map(List<UserEntity>::toDto)
        .flowOn(Dispatchers.Default)
    override val jobs = jobDao.getJobs()
        .map(List<JobEntity>::toDto)
        .flowOn(Dispatchers.Default)


    override suspend fun getPosts() {
        try {
            val response = postsApiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val post = bodyResponse.map {
                Post(
                    it.id,
                    it.authorId,
                    it.author,
                    it.authorAvatar,
                    it.authorJob,
                    it.content,
                    it.published,
                    it.coordinates,
                    it.link,
                    it.likeOwnerIds,
                    it.mentionIds,
                    it.mentionIds?.map { id ->
                        it.users?.get(id.toString())!!.name
                    },
                    it.mentionedMe,
                    it.likedByMe,
                    it.attachment,
                    it.ownedByMe
                )
            }
            val users = bodyResponse.map {
                it.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            }
            postDao.insert(post.toEntity())
            users.map {
                if (it != null) {
                    userDao.insert(it.toEntity())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getPostsByAuthor(userId: Long) {
        try {
            val response = postsApiService.getPostsByAuthor(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val post = bodyResponse.map {
                Post(
                    it.id,
                    it.authorId,
                    it.author,
                    it.authorAvatar,
                    it.authorJob,
                    it.content,
                    it.published,
                    it.coordinates,
                    it.link,
                    it.likeOwnerIds,
                    it.mentionIds,
                    it.mentionIds?.map { id ->
                        it.users?.get(id.toString())!!.name
                    },
                    it.mentionedMe,
                    it.likedByMe,
                    it.attachment,
                    it.ownedByMe
                )
            }
            val users = bodyResponse.map {
                it.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            }
            postDao.insert(post.toEntity())
            users.map {
                if (it != null) {
                    userDao.insert(it.toEntity())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaRequest): MediaResponse {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = postsApiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val postRequest = PostRequest(
                post.id, post.content,
                post.coordinates,
                post.link,
                post.attachment, post.mentionIds
            )
            val response = postsApiService.save(postRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val postResponse = Post(
                bodyResponse.id,
                bodyResponse.authorId,
                bodyResponse.author,
                bodyResponse.authorAvatar,
                bodyResponse.authorJob,
                bodyResponse.content,
                bodyResponse.published,
                bodyResponse.coordinates,
                bodyResponse.link,
                bodyResponse.likeOwnerIds,
                bodyResponse.mentionIds,
                bodyResponse.mentionIds?.map { id ->
                    bodyResponse.users?.get(id.toString())!!.name
                },
                bodyResponse.mentionedMe,
                bodyResponse.likedByMe,
                bodyResponse.attachment,
                bodyResponse.ownedByMe
            )
            val users =
                bodyResponse.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }

            postDao.insert(PostEntity.fromDto(postResponse))
            users?.map {
                userDao.insert(UserEntity.fromDto(it))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaRequest) {
        try {
            val media = upload(upload)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = postsApiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = postsApiService.getById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val post =
                response.body()?.let {
                    Post(
                        it.id,
                        it.authorId,
                        it.author,
                        it.authorAvatar,
                        it.authorJob,
                        it.content,
                        it.published,
                        it.coordinates,
                        it.link,
                        it.likeOwnerIds,
                        it.mentionIds,
                        it.mentionIds?.map { id ->
                            it.users?.get(id.toString())!!.name
                        },
                        it.mentionedMe,
                        it.likedByMe,
                        it.attachment,
                        it.ownedByMe
                    )
                } ?: throw ApiError(response.code(), response.message())

            if (post.likedByMe) {
                val dislikedPost = post.copy(likedByMe = false)
                postDao.insert(PostEntity.fromDto(dislikedPost))
                val response2 = postsApiService.dislikeById(id)
                if (!response2.isSuccessful) {
                    throw ApiError(response2.code(), response2.message())
                }
            } else {
                val likedPost = post.copy(likedByMe = true)
                postDao.insert(PostEntity.fromDto(likedPost))
                val response2 = postsApiService.likeById(id)
                if (!response2.isSuccessful) {
                    throw ApiError(response2.code(), response2.message())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun userAuthentication(login: String, pass: String): AuthState {
        try {
            val authResponse = authApi.authUser(login, pass)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val userById = authApi.getUserById(authResponse.body()?.id)

            if (!userById.isSuccessful) {
                throw ApiError(userById.code(), userById.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token
            val name = userById.body()?.name

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun userRegistration(
        login: String,
        pass: String,
        name: String
    ): AuthState {
        try {
            val authResponse = authApi.userRegist(login, pass, name)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun userRegistrationWithAvatar(
        login: String,
        pass: String,
        name: String,
        avatar: MediaRequest
    ): AuthState {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", avatar.file.name, avatar.file.asRequestBody()
            )

            val authResponse = authApi.userRegistWithAvatar(login, pass, name, media)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getEvents() {
        try {
            val response = eventApiService.getEvents()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val users = bodyResponse.map {
                it.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            }
            users.map {
                if (it != null) {
                    userDao.insert(it.toEntity())
                }
            }
            val event = bodyResponse.map {
                Event(
                    it.id,
                    it.authorId,
                    it.author,
                    it.authorAvatar,
                    it.authorJob,
                    it.content,
                    it.datetime,
                    it.published,
                    it.coordinates,
                    it.type,
                    it.likeOwnerIds,
                    it.likedByMe,
                    it.speakerIds,
                    it.speakerIds?.map { id ->
                        it.users?.get(id.toString())!!.name
                    },
                    it.participantsIds,
                    it.participantsIds?.map { id ->
                        it.users?.get(id.toString())!!.name
                    },
                    it.participatedByMe,
                    it.attachment,
                    it.link,
                    it.ownedByMe
                )
            }
            eventDao.insert(event.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEvent(event: Event) {
        try {
            val eventRequest = EventRequest(
                event.id,
                event.content,
                event.datetime,
                event.coordinates,
                event.type,
                event.attachment,
                event.link,
                event.speakerIds
            )
            val response = eventApiService.saveEvent(eventRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val eventResponse = Event(
                bodyResponse.id,
                bodyResponse.authorId,
                bodyResponse.author,
                bodyResponse.authorAvatar,
                bodyResponse.authorJob,
                bodyResponse.content,
                bodyResponse.datetime,
                bodyResponse.published,
                bodyResponse.coordinates,
                bodyResponse.type,
                bodyResponse.likeOwnerIds,
                bodyResponse.likedByMe,
                bodyResponse.speakerIds,
                bodyResponse.speakerIds?.map { id ->
                    bodyResponse.users?.get(id.toString())!!.name
                },
                bodyResponse.participantsIds,
                bodyResponse.participantsIds?.map { id ->
                    bodyResponse.users?.get(id.toString())!!.name
                },
                bodyResponse.participatedByMe,
                bodyResponse.attachment,
                bodyResponse.link,
                bodyResponse.ownedByMe
            )
            val users =
                bodyResponse.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            eventDao.insert(EventEntity.fromDto(eventResponse))
            users?.map {
                userDao.insert(UserEntity.fromDto(it))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEventWithAttachment(event: Event, upload: MediaRequest) {
        try {
            val media = upload(upload)
            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            saveEvent(eventWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeEventById(id: Long, likedByMe: Boolean) {
        try {
            if (likedByMe) {
                val response = eventApiService.dislikeEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.let {
                        Event(
                            it.id,
                            it.authorId,
                            it.author,
                            it.authorAvatar,
                            it.authorJob,
                            it.content,
                            it.datetime,
                            it.published,
                            it.coordinates,
                            it.type,
                            it.likeOwnerIds,
                            it.likedByMe,
                            it.speakerIds,
                            it.speakerIds?.map { id ->
                                it.users?.get(id.toString())!!.name
                            },
                            it.participantsIds,
                            it.participantsIds?.map { id ->
                                it.users?.get(id.toString())!!.name
                            },
                            it.participatedByMe,
                            it.attachment,
                            it.link,
                            it.ownedByMe
                        )
                    } ?: throw ApiError(response.code(), response.message())
                eventDao.insert(EventEntity.fromDto(event))
            } else {
                val response = eventApiService.likeEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.let {
                        Event(
                            it.id,
                            it.authorId,
                            it.author,
                            it.authorAvatar,
                            it.authorJob,
                            it.content,
                            it.datetime,
                            it.published,
                            it.coordinates,
                            it.type,
                            it.likeOwnerIds,
                            it.likedByMe,
                            it.speakerIds,
                            it.speakerIds?.map { id ->
                                it.users?.get(id.toString())!!.name
                            },
                            it.participantsIds,
                            it.participantsIds?.map { id ->
                                it.users?.get(id.toString())!!.name
                            },
                            it.participatedByMe,
                            it.attachment,
                            it.link,
                            it.ownedByMe
                        )
                    } ?: throw ApiError(response.code(), response.message())
                eventDao.insert(EventEntity.fromDto(event))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeEventById(id: Long) {
        try {
            eventDao.removeById(id)
            val response = eventApiService.removeEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun partEventById(id: Long, participatedByMe: Boolean) {
        try {
            if (participatedByMe) {
                val response = eventApiService.nonPartEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.let {
                        Event(
                            it.id,
                            it.authorId,
                            it.author,
                            it.authorAvatar,
                            it.authorJob,
                            it.content,
                            it.datetime,
                            it.published,
                            it.coordinates,
                            it.type,
                            it.likeOwnerIds,
                            it.likedByMe,
                            it.speakerIds,
                            it.speakerIds?.map { id ->
                                it.users?.get(id.toString())!!.name
                            },
                            it.participantsIds,
                            it.participantsIds?.map { id ->
                                it.users?.get(id.toString())!!.name
                            },
                            it.participatedByMe,
                            it.attachment,
                            it.link,
                            it.ownedByMe
                        )
                    } ?: throw ApiError(response.code(), response.message())
                eventDao.insert(EventEntity.fromDto(event))
            } else {
                val response = eventApiService.partEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.let {
                        Event(
                            it.id,
                            it.authorId,
                            it.author,
                            it.authorAvatar,
                            it.authorJob,
                            it.content,
                            it.datetime,
                            it.published,
                            it.coordinates,
                            it.type,
                            it.likeOwnerIds,
                            it.likedByMe,
                            it.speakerIds,
                            it.speakerIds?.map { id ->
                                it.users?.get(id.toString())!!.name
                            },
                            it.participantsIds,
                            it.participantsIds?.map { id ->
                                it.users?.get(id.toString())!!.name
                            },
                            it.participatedByMe,
                            it.attachment,
                            it.link,
                            it.ownedByMe
                        )
                    } ?: throw ApiError(response.code(), response.message())
                eventDao.insert(EventEntity.fromDto(event))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUsers() {
        try {
            val response = authApi.getUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val users = bodyResponse.map {
                Users(
                    it.id,
                    it.name,
                    it.avatar
                )
            }
            userDao.insert(users.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getJobs(userId: Long, currentUser: Long) {
        try {
            val response = jobApiService.getJobsByUserId(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val job = bodyResponse.map {
                Job(
                    userId,
                    userId == currentUser,
                    it.id,
                    it.name,
                    it.position,
                    it.start,
                    it.finish,
                    it.link,
                )
            }
            jobDao.insert(job.toEntity())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveJob(userId : Long, job : Job) {
        try {
            val jobRequest = JobRequest(
                job.id,
                job.name,
                job.position,
                job.start,
                job.finish,
                job.link
            )
            val response = jobApiService.saveJob(jobRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val job2save = Job(
                userId,
                true,
                bodyResponse.id,
                bodyResponse.name,
                bodyResponse.position,
                bodyResponse.start,
                bodyResponse.finish,
                bodyResponse.link,
            )
            jobDao.insert(JobEntity.fromDto(job2save))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override suspend fun removeJobById(id: Long) {
        try {
            jobDao.removeById(id)
            val response = jobApiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}