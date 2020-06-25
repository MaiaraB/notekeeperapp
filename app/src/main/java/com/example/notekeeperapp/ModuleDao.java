package com.example.notekeeperapp;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ModuleDao {

    @Insert
    public long insertModule(ModuleInfo moduleInfo);

    @Update
    public int updateModule(ModuleInfo... moduleInfos);

    @Delete
    public int deleteCourse(CourseInfo moduleInfo);

    @Query("SELECT * FROM module_info")
    public LiveData<List<ModuleInfo>> getAllModulesFromCourse();
}
