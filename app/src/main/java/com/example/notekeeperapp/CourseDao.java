package com.example.notekeeperapp;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public interface CourseDao {

    @Insert
    public long insertCourse(CourseInfo courseInfo);

    @Update
    public int updateCourse(CourseInfo courseInfo);

    @Delete
    public int deleteCourse(CourseInfo courseInfo);

    @Query("SELECT * FROM course_info")
    public LiveData<List<CourseInfo>> getAllCourses();

    @Query("SELECT * FROM course_info WHERE id = :id")
    public LiveData<CourseInfo> getCourse(long id);

    @Transaction
    @Query("SELECT * FROM course_info")
    public LiveData<List<CourseWithNotes>> getAllCoursesWithNotes();

    @Transaction
    @Query("SELECT * FROM course_info WHERE id = :id")
    public LiveData<CourseWithNotes> getCourseWithNotes(long id);

    @Transaction
    @Query("SELECT * FROM course_info")
    public LiveData<List<CourseWithModules>> getAllCourseWithModules();

    @Transaction
    @Query("SELECT * FROM course_info WHERE id = :id")
    public LiveData<CourseWithModules> getCourseWithModules(long id);
}
