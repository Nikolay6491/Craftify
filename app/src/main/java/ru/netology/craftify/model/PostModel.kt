package ru.netology.craftify.model

import ru.netology.craftify.dto.Attachment

data class PostModel(
    val id: Long = 0L,
    val author: String = "",
    val content: String = "",
    val published: String = "",
    var likedByMe: Boolean = false,
    val likes: Int = 0,
    val authorAvatar: String = "",
    val isHidden: Boolean = false,
    val attachment: Attachment? = null
)