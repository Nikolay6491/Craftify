package ru.netology.craftify.entity

import ru.netology.craftify.dto.Attachment
import ru.netology.craftify.type.AttachmentType

data class AttachmentEmbeddable(
    var url: String,
    var attachmentType: AttachmentType,
) {
    fun toDto() = Attachment(url, attachmentType)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}