package ru.netology.craftify.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.craftify.dto.Attachment
import ru.netology.craftify.dto.Post
import ru.netology.craftify.type.AttachmentType

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,

    val authorId: Long,

    val authorAvatar: String,
    val content: String,
    val published: String,
    val likes: Long = 0,
    val shares: Long,
    val views: Long,
    val likedByMe: Boolean,
    val sharesByMe: Boolean,
    val video: String? = null,

    val hidden: Boolean = true,
    @Embedded
    val attachment: AttachmentEmbeddable?,
) {
    fun toDto() = Post(
        id = id,
        author = author,
        authorId = authorId,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        likes = likes,
        shares = shares,
        views = views,
        likedByMe = likedByMe,
        sharesByMe = sharesByMe,
        video = video,
        hidden = hidden,
        attachment = attachment?.toDto(),
        ownedByMe = false)

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                author = dto.author,
                authorId = dto.authorId,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                likes = dto.likes,
                shares = dto.shares,
                views = dto.views,
                likedByMe = dto.likedByMe,
                sharesByMe = dto.sharesByMe,
                video = dto.video,
                hidden = dto.hidden,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
            )
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)