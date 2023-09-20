package ru.netology.craftify.dao

import androidx.room.TypeConverter
import ru.netology.craftify.type.AttachmentType

class Converters {
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

    @TypeConverter
    fun fromListDto(list: List<Long>?): String {
        if (list == null) return ""
        return list.toString()
    }

    @TypeConverter
    fun fromListStringDto(list: List<String>?): String {
        if (list == null) return ""
        return list.toString()
    }

    @TypeConverter
    fun toListDto(data: String?): List<Long>? {
        if (data == "[]") return emptyList<Long>()
        else {
            val substring = data?.substring(1, data.length - 1)
            return substring?.split(", ")?.map { it.toLong() }
        }
    }

    @TypeConverter
    fun toListStringDto(data: String?): List<String>? {
        if (data == "[]") return emptyList<String>()
        else {
            val substring = data?.substring(1, data.length - 1)
            return substring?.split(", ")?.map { it.toString() }
        }
    }
}