package com.example.notekeeperapp;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NoteActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final NoteKeeperAppDatabase mDb;
    private final long mNoteId;

    public NoteActivityViewModelFactory(NoteKeeperAppDatabase db, long noteId) {
        mDb = db;
        mNoteId = noteId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NoteActivityViewModel(mDb, mNoteId);
    }
}
