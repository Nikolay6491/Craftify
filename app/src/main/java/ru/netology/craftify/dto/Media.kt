package ru.netology.craftify.dto

import java.io.File

data class MediaResponse(val url: String)

data class MediaRequest(val file: File)