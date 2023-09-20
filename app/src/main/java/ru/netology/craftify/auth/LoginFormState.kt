package ru.netology.craftify.auth

data class LoginFormState(
    val isDataValid: Boolean = false,
    val isLoading: Boolean = false,
    val isError : Boolean = false
)