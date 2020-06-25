package com.example.notekeeperapp;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface NoteDao {

    @Insert
    public long insertNote(NoteInfo noteInfo);

    @Update
    public int updateNote(NoteInfo noteInfo);

    @Delete
    public int deleteNote(NoteInfo noteInfo);

    @Query("SELECT * FROM note_info WHERE id = :id")
    public LiveData<NoteInfo> getNote(long id);

    @Query("SELECT * FROM NoteInfoExpanded ORDER BY course_title, note_title")
    public LiveData<List<NoteInfoExpanded>> getAllNotesExpanded();

    @Query("SELECT COUNT(*) FROM note_info")
    public LiveData<Integer> getNotesCount();
}
