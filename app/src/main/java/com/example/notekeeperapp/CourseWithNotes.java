package com.example.notekeeperapp;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

public class CourseWithNotes {

    @Embedded
    public CourseInfo mCourse;
    @Relation(
            parentColumn = "id",
            entityColumn = "course_id"
    )
    public List<NoteInfo> mNotes;
}
