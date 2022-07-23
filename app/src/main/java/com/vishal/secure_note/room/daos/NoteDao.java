package com.vishal.secure_note.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.vishal.secure_note.room.entities.NoteEntity;

import java.util.List;

/**
 * Created by Vishal on 12, Jul, 2022
 */

@Dao
public interface NoteDao {
    @Insert
    void insertNote(NoteEntity note);

    @Query("UPDATE noteentity SET noteTitle=:newTitle WHERE uid=:id")
    void updateNoteTitle(int id, String newTitle);

    @Query("UPDATE noteentity SET noteDescription=:newDescription WHERE uid=:id")
    void updateNoteDescription(int id, String newDescription);

    @Query("UPDATE noteentity SET isPrivate=:isPrivate WHERE uid=:id")
    void updateVisibility(int id, boolean isPrivate);

    @Query("UPDATE noteentity SET label=:label WHERE uid=:id")
    void updateLabel(int id, int label);

    @Query("UPDATE noteentity SET password=:password WHERE uid=:id")
    void updatePassword(int id, String password);

    @Query("UPDATE noteentity SET passwordType=:passwordType WHERE uid=:id")
    void updatePasswordType(int id, int passwordType);

    @Query("SELECT * FROM noteentity")
    LiveData<List<NoteEntity>> getAllNotesLiveList();

    @Query("SELECT * FROM noteentity WHERE noteTitle LIKE '%'|| :searchStr || '%' OR noteDescription LIKE '%'|| :searchStr || '%'")
    LiveData<List<NoteEntity>> getSearchResult(String searchStr);

    @Query("SELECT * FROM noteentity WHERE uid=:id")
    LiveData<NoteEntity> getNoteById(int id);

    @Delete
    void deleteNote(NoteEntity note);
}
