package com.example.notekeeperapp;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeperapp.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeperapp.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeperapp.ORIGINAL_NOTE_TEXT";
    public static final String NOTE_URI = "com.example.notekeeperapp.NOTE_URI";

    public String mOriginalNoteCourseId;
    public String mOriginalNoteTitle;
    public String mOriginalNoteText;
    public String mNoteUri;
    public boolean isNewlyCreated = true;

    public void saveState(Bundle outState) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
        outState.putString(NOTE_URI, mNoteUri);
    }

    public void restoreState(Bundle inState) {
        mOriginalNoteCourseId = inState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = inState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = inState.getString(ORIGINAL_NOTE_TEXT);
        mNoteUri = inState.getString(NOTE_URI);
    }
}
