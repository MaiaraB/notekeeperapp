package com.example.notekeeperapp;

import android.os.AsyncTask;
import android.os.Bundle;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {

    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeperapp.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeperapp.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeperapp.ORIGINAL_NOTE_TEXT";
    public static final String NOTE_URI = "com.example.notekeeperapp.NOTE_URI";

    public long mOriginalNoteCourseId;
    public String mOriginalNoteTitle;
    public String mOriginalNoteText;
    public boolean isNewlyCreated = true;
    public boolean[] mModuleStatuses;

    private NoteKeeperAppDatabase mDb;
    private NoteDao mNoteDao;
    private CourseDao mCourseDao;
    private ModuleDao mModuleDao;
    private LiveData<List<CourseInfo>> mCourses;
    private LiveData<Integer> mNotesCount;
    private LiveData<NoteInfo> mNote;
    private LiveData<CourseWithModules> mCourseWithModules;
    private long mNoteId;

    public NoteActivityViewModel(NoteKeeperAppDatabase db, long noteId) {
        mDb = db;
        mNoteDao = mDb.noteDao();
        mCourseDao = mDb.courseDao();
        mModuleDao = mDb.moduleDao();
        mCourses = mCourseDao.getAllCourses();
        mNotesCount = mNoteDao.getNotesCount();
        mNoteId = noteId;
        mNote = mNoteDao.getNote(mNoteId);
    }

    public LiveData<List<CourseInfo>> fetchSpinnerCourses() {
        return mCourses;
    }

    public LiveData<Integer> getNotesCount() {
        return mNotesCount;
    }

    public LiveData<NoteInfo> getNote() {
        return mNote;
    }

    public LiveData<NoteInfo> setNoteId(long noteId) {
        mNoteId = noteId;
        mNote = mNoteDao.getNote(mNoteId);
        return mNote;
    }

    public void updateNote(final NoteInfo note) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                mNoteDao.updateNote(note);
                return null;
            }
        };
        task.execute();
    }

    public long createNote(NoteInfo note) {
        return mNoteDao.insertNote(note);
    }

    public void deleteNote(NoteInfo note) {
        final NoteInfo noteCopy = new NoteInfo(note.getId(), note.getCourseId(), note.getTitle(), note.getText());
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                mNoteDao.deleteNote(noteCopy);
                return null;
            }
        };
        task.execute();
    }

    public LiveData<CourseWithModules> getNoteCourseModules(long courseId) {
        mCourseWithModules = mCourseDao.getCourseWithModules(courseId);
        return mCourseWithModules;
    }

    public void updateModules(ModuleInfo... moduleInfos) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                mModuleDao.updateModule(moduleInfos);
                return null;
            }
        };
        task.execute();
    }

    public void saveState(Bundle outState) {
        outState.putLong(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    public void restoreState(Bundle inState) {
        mOriginalNoteCourseId = inState.getLong(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = inState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = inState.getString(ORIGINAL_NOTE_TEXT);
    }
}
