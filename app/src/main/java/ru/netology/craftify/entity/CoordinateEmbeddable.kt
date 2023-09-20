package ru.netology.craftify.entity

import ru.netology.craftify.dto.Coordinates

data class CoordinateEmbeddable(
    val latitude : String?,
    val longitude : String?,
) {
    fun toDto() =
        Coordinates(latitude, longitude)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinateEmbeddable(it.lat, it.long)
        }
    }
}