package ru.netology.craftify.model

import ru.netology.craftify.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)

sealed interface FeedModelState {
    object Idle : FeedModelState
    object Error : FeedModelState
    object Loading : FeedModelState

    object Refresh : FeedModelState

}