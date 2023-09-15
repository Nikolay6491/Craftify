package ru.netology.craftify.repository

import android.util.Log
import androidx.paging.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.craftify.api.PostsApiService
import ru.netology.craftify.dao.PostDao
import ru.netology.craftify.dao.PostRemoteKeyDao
import ru.netology.craftify.db.AppDb
import ru.netology.craftify.dto.*
import ru.netology.craftify.entity.PostEntity
import ru.netology.craftify.entity.toEntity
import ru.netology.craftify.error.ApiError
import ru.netology.craftify.error.AppError
import ru.netology.craftify.error.NetworkError
import ru.netology.craftify.error.UnknownError
import ru.netology.craftify.type.AttachmentType

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: PostsApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override var data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {postDao.getPagingSource()},
        remoteMediator = PostRemoteMediator(
            postsApiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map {
            it.map (PostEntity::toDto)
                .insertSeparators { previous, _ ->
                    if (previous?.id?.rem(5) == 0L) {
                        Ad(Random.nextLong(),"figma.jpg")
                    } else {
                        null
                    }
                }
        }

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(posts.map(PostEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getById(id: Long?): Post? {
        return if (id != null) postDao.getById(id).toDto() else null

    }

    override suspend fun likes(id: Long, likesByMe: Boolean) {
        try {
            val response = apiService.like(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(posts))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun sharesById(id: Long) {
        Log.e("PostRepositoryImpl", "Share is not yet implemented")
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.remove(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            response.body() ?: throw ApiError(response.code(), response.message())
            postDao.removeById(id)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post)

            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            response.body() ?: throw ApiError(response.code(), response.message())
            postDao.save(PostEntity.fromDto(post))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = uploadMedia(upload)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun uploadMedia(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.uploadMedia(media)
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

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity().map {
                it.copy(hidden = true)
            })

            emit(body.size)

        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)


    override suspend fun showAll() {
        postDao.showAll()

    }
}