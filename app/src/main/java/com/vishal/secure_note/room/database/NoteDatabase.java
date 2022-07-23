package com.vishal.secure_note.room.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.vishal.secure_note.room.daos.NoteDao;
import com.vishal.secure_note.room.entities.NoteEntity;

/**
 * Created by Vishal on 12, Jul, 2022
 */

@Database(entities = {NoteEntity.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();

    private static volatile NoteDatabase INSTANCE;

    public static NoteDatabase getInstance(final Context context) {
        synchronized (NoteDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        NoteDatabase.class,
                        "Notes-Secure").build();
            }
        }
        return INSTANCE;
    }

}
