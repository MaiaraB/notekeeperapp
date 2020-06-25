package com.example.notekeeperapp;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

public class CourseWithModules {
    @Embedded
    public CourseInfo mCourseInfo;
    @Relation(
            parentColumn = "id",
            entityColumn = "course_id"
    )
    public List<ModuleInfo> mModules;
}
