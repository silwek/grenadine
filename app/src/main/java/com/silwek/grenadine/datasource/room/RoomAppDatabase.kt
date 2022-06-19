package com.silwek.grenadine.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.silwek.grenadine.datasource.room.dao.NoteDao
import com.silwek.grenadine.datasource.room.entities.NoteEntity
import com.silwek.grenadine.models.secondToLocalDateTime
import com.silwek.grenadine.models.toSecond
import java.time.LocalDateTime

@Database(entities = [NoteEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class RoomAppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.secondToLocalDateTime()
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toSecond()
    }
}
