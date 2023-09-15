package ru.netology.craftify.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.craftify.dto.FeedItem
import ru.netology.craftify.dto.Media
import ru.netology.craftify.dto.MediaUpload
import ru.netology.craftify.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    suspend fun getById(id: Long?): Post?
    suspend fun likes(id: Long, likesByMe: Boolean)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun uploadMedia(upload: MediaUpload): Media
    suspend fun sharesById(id: Long)
    suspend fun removeById(id: Long)
    fun getNewer(id: Long): Flow<Int>
    suspend fun showAll()
}