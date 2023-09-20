package ru.netology.craftify.dto

data class Users(
    override val id: Long,
    val name: String,
    val avatar: String? = null,
) : FeedItem

data class UserPreview(
    val name : String,
    val avatar : String? = null,
)

data class UserResponse(
    override val id: Long = 0,
    val login: String,
    val name: String,
    val avatar: String? = null
) : FeedItem