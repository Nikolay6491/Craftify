package ru.netology.craftify.model

import ru.netology.craftify.dto.Event

data class EventModel(
    val events: List<Event> = emptyList(),
    val empty: Boolean = false,
)