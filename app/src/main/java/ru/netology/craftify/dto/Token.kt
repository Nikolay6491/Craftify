package ru.netology.craftify.dto

data class Token(
    val id: Long = 0L,
    val token: String? = null,
    val name: String? = null,
    val avatar: String? = null,
)