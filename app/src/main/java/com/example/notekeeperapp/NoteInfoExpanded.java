package com.example.notekeeperapp;

import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;

@DatabaseView("SELECT note_info.id, note_info.note_title, course_info.course_title AS course_title" +
        " FROM note_info INNER JOIN course_info ON note_info.course_id = course_info.id")
public class NoteInfoExpanded {
    @ColumnInfo(name = "id")
    private int mId;
    @ColumnInfo(name = "note_title")
    private String mNoteTitle;
    @ColumnInfo(name = "course_title")
    private String mCourseTitle;

    public int getId() { return mId; }

    public void setId(int id) { mId = id; }

    public String getNoteTitle() {
        return mNoteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        mNoteTitle = noteTitle;
    }

    public String getCourseTitle() {
        return mCourseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        mCourseTitle = courseTitle;
    }
}
