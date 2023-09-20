package ru.netology.craftify.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.craftify.dao.*
import ru.netology.craftify.entity.*

@Database(
    entities = [PostEntity::class, EventEntity::class, UserEntity::class, JobEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
}