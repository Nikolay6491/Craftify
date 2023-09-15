package ru.netology.craftify.model

data class AuthModel(
    val loading: Boolean = false,
    val error: Boolean = false,
    val success: Boolean = false
)