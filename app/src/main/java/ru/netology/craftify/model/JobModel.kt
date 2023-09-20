package ru.netology.craftify.model

import ru.netology.craftify.dto.Job

data class JobModel(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false,
)