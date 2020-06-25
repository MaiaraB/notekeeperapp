package com.example.notekeeperapp;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainActivityViewModel extends AndroidViewModel {

    private NoteDao mNoteDao;
    private CourseDao mCourseDao;
    private LiveData<List<NoteInfoExpanded>> mNotesExpanded;
    private LiveData<List<CourseInfo>> mCourses;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);

        NoteKeeperAppDatabase db = NoteKeeperAppDatabase.getInstance(application);
        mNoteDao = db.noteDao();
        mCourseDao = db.courseDao();
        mNotesExpanded = mNoteDao.getAllNotesExpanded();
        mCourses = mCourseDao.getAllCourses();
    }

    public LiveData<List<NoteInfoExpanded>> getNotesExpanded() {
        return mNotesExpanded;
    }

    public LiveData<List<CourseInfo>> getCourses() { return mCourses; }
}
