package ru.netology.craftify.dto

import ru.netology.craftify.type.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType
)