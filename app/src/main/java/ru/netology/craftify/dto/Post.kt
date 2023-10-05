package ru.netology.craftify.dto

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob : String? = null,
    val content: String,
    val published: String,
    val coords : Coordinates? = null,
    val link : String? = null,
    val likeOwnerIds : List<Long>? = emptyList(),
    val mentionIds : List<Long>? = emptyList(),
    val mentionList : List<String>? = emptyList(),
    val mentionedMe : Boolean = false,
    val likedByMe : Boolean = false,
    val attachment : Attachment? = null,
    val ownedByMe : Boolean = false,
) : FeedItem

data class PostRequest(
    override val id : Long,
    val content: String,
    val coords : Coordinates? = null,
    val link : String? = null,
    val attachment : Attachment? = null,
    val mentionIds : List<Long>? = emptyList(),
) : FeedItem

data class PostResponse(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob : String? = null,
    val content: String,
    val published: String,
    val coords : Coordinates? = null,
    val link : String? = null,
    val likeOwnerIds : List<Long>? = emptyList(),
    val mentionIds : List<Long>? = emptyList(),
    val mentionedMe : Boolean = false,
    val likedByMe : Boolean = false,
    val attachment : Attachment? = null,
    val ownedByMe : Boolean = false,
    val users : Map<String, UserPreview>?,
) : FeedItem