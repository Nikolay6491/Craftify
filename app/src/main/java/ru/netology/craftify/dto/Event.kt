package ru.netology.craftify.dto

import ru.netology.craftify.type.EventType

data class Event(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates? = null,
    val type: EventType,
    val likeOwnerIds: List<Long>? = emptyList(),
    val likedByMe: Boolean,
    val speakerIds: List<Long>? = emptyList(),
    val speakerList: List<String>? = emptyList(),
    val participantsIds: List<Long>? = emptyList(),
    val participantsList: List<String>? = emptyList(),
    val participatedByMe: Boolean,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean,
) : FeedItem

data class EventRequest(
    override val id: Long,
    val content: String,
    val datetime: String?,
    val coords: Coordinates?,
    val type: EventType?,
    val attachment: Attachment?,
    val link: String?,
    val speakerIds: List<Long>?
) : FeedItem

data class EventResponse(
    override val id : Long,
    val authorId : Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob : String?,
    val content : String,
    val datetime : String,
    val published : String,
    val coords : Coordinates?,
    val type : EventType,
    val likeOwnerIds : List<Long>? = emptyList(),
    val likedByMe : Boolean,
    val speakerIds : List<Long>? = emptyList(),
    val participantsIds : List<Long>? = emptyList(),
    val participatedByMe : Boolean,
    val attachment: Attachment?,
    val link : String?,
    val ownedByMe : Boolean,
    val users : Map<String, UserPreview>?,
) : FeedItem