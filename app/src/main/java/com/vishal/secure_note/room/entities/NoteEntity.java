package com.vishal.secure_note.room.entities;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Vishal on 11, Jul, 2022
 */

@Entity
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    private int uid;
    private String noteTitle, noteDescription;
    private long noteDate;
    private boolean isPrivate;
    private String password;
    /**
     * Label will be in integer form and all value means some priority
     * label 0 == NORMAL
     * label 1 == NEEDED
     * label 2 == VERY IMPORTANT
     * And after some time user can create own label if needed!
     */
    private int label;

    // which password is set mobile, app's or custom password
    private int passwordType;

    public NoteEntity(String noteTitle, String noteDescription, long noteDate, boolean isPrivate, String password, int label, int passwordType) {
        this.noteTitle = noteTitle;
        this.noteDescription = noteDescription;
        this.noteDate = noteDate;
        this.isPrivate = isPrivate;
        this.password = password;
        this.label = label;
        this.passwordType = passwordType;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteDescription() {
        return noteDescription;
    }

    public void setNoteDescription(String noteDescription) {
        this.noteDescription = noteDescription;
    }

    public long getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(long noteDate) {
        this.noteDate = noteDate;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public int getPasswordType() {
        return passwordType;
    }

    public void setPasswordType(int passwordType) {
        this.passwordType = passwordType;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof NoteEntity) {
            NoteEntity note = (NoteEntity) obj;
            return note.getNoteTitle().equals(this.noteTitle) &&
                    note.getNoteDescription().equals(this.noteDescription) &&
                    note.getLabel() == this.label && note.isPrivate == this.isPrivate &&
                    note.getPasswordType() == this.passwordType && note.password.equals(this.password);
        }
        return false;
    }
}
