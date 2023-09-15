package ru.netology.craftify.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.craftify.api.PostsApiService
import ru.netology.craftify.dao.PostDao
import ru.netology.craftify.dao.PostRemoteKeyDao
import ru.netology.craftify.db.AppDb
import ru.netology.craftify.entity.PostEntity
import ru.netology.craftify.entity.PostRemoteKeyEntity
import ru.netology.craftify.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val postsApiService: PostsApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    postRemoteKeyDao.max()?.let {
                        postsApiService.getAfter(it, state.config.pageSize)
                    } ?: postsApiService.getLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max()
                        ?: return MediatorResult.Success(false)
                    postsApiService.getAfter(id, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    postsApiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }

            val body = result.body() ?: throw ApiError(
                result.code(),
                result.message()
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postDao.clear()
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            ),
                        )
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            ),
                        )
                        postDao.insert(body.map(PostEntity::fromDto))
                    }
                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id,
                            ),
                        )
                        postDao.insertScroll(body.map(PostEntity::fromDto))
                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            ),
                        )
                        postDao.insertScroll(body.map(PostEntity::fromDto))
                    }
                }
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

}