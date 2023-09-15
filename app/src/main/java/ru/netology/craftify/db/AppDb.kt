package ru.netology.craftify.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.craftify.dao.Converters
import ru.netology.craftify.dao.PostDao
import ru.netology.craftify.dao.PostRemoteKeyDao
import ru.netology.craftify.entity.PostEntity
import ru.netology.craftify.entity.PostRemoteKeyEntity

@Database(
    entities = [PostEntity::class, PostRemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}