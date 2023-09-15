package ru.netology.craftify.dao

import androidx.paging.PagingSource
import androidx.room.*

import kotlinx.coroutines.flow.Flow
import ru.netology.craftify.entity.PostEntity
import ru.netology.craftify.type.AttachmentType

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getById(id: Long): PostEntity

    @Query("SELECT * FROM PostEntity WHERE hidden=0 ORDER BY id DESC")
    fun getAllVisible(): Flow<List<PostEntity>>

    @Query("UPDATE PostEntity SET hidden = 0 ")
    suspend fun showAll()

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun markRead(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity WHERE hidden=0")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScroll(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)


    @Query("""
       UPDATE PostEntity SET
        likes = likes + CASE WHEN likes THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id;
    """)

    suspend fun likesById(id: Long)


    @Query("""
       UPDATE PostEntity SET
        shares = shares + 1
        WHERE id = :id;
    """)
    suspend fun sharesById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}

class Converters {
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

}