package ru.netology.craftify.dto

data class Job(
    val userId : Long,
    val ownedByMe : Boolean = false,
    override val id : Long,
    val name : String,
    val position : String,
    val start : String,
    val finish : String? = null,
    val link : String? = null,
) : FeedItem

data class JobRequest(
    override val id : Long,
    val name : String,
    val position : String,
    val start : String,
    val finish : String?,
    val link : String?
) : FeedItem

data class JobResponse(
    override val id : Long,
    val name : String,
    val position : String,
    val start : String,
    val finish : String?,
    val link : String?
) : FeedItem