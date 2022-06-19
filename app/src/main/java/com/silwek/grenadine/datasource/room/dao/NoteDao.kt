package com.silwek.grenadine.datasource.room.dao

import androidx.room.*
import com.silwek.grenadine.datasource.room.entities.NoteEntity

@Dao
interface NoteDao {
    @Query("SELECT * FROM noteentity")
    fun getAll(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: NoteEntity):Long

    @Update
    fun update(note: NoteEntity)

    @Delete
    fun delete(note: NoteEntity)

    @Delete
    fun deleteNotes(vararg note: NoteEntity)
}