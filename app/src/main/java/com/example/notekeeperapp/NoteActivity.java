package com.example.notekeeperapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.notekeeperapp.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeperapp.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeperapp.NoteKeeperProviderContract.Courses;
import com.example.notekeeperapp.NoteKeeperProviderContract.Notes;
import com.google.android.material.snackbar.Snackbar;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String NOTE_ID = "com.example.notekeeperapp.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    public static final int NOTIFICATION_ID = 888;
    private final String TAG = getClass().getSimpleName();
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCoursers;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteId;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;
    private Uri mNoteUri;
    private ModuleStatusView mViewModuleStatus;
    //    private NotificationManagerCompat mNotificationManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

//        mNotificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if (mViewModel.isNewlyCreated && savedInstanceState != null) {
            mViewModel.restoreState(savedInstanceState);
            String stringNoteUri = savedInstanceState.getString(NoteActivityViewModel.NOTE_URI);
            mNoteUri = Uri.parse(stringNoteUri);
        }

        mViewModel.isNewlyCreated = false;

        mSpinnerCoursers = findViewById(R.id.spinner_courses);

        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[] {android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCoursers.setAdapter(mAdapterCourses);

//        loadCourseData();
        getSupportLoaderManager().initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();
//        saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote)
            getSupportLoaderManager().initLoader(LOADER_NOTES, null, this);

        mViewModuleStatus = (ModuleStatusView) findViewById(R.id.module_status);
        loadModuleStatusValues();
    }

    private void loadModuleStatusValues() {
        // In real life we'd lookup the selected course's module statuses from the content provider
        int totalNumberOfModules = 11;
        int completedNumberOfModules = 7;
        boolean[] moduleStatus = new boolean[totalNumberOfModules];

        for (int moduleIndex = 0; moduleIndex < completedNumberOfModules; moduleIndex++)
            moduleStatus[moduleIndex] = true;

        mViewModuleStatus.setModuleStatus(moduleStatus);
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        mAdapterCourses.changeCursor(cursor);
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;

//        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
//        mViewModel.mOriginalNoteTitle = mNote.getTitle();
//        mViewModel.mOriginalNoteText = mNote.getText();
        mViewModel.mOriginalNoteCourseId = mNoteCursor.getString(mCourseIdPos);
        mViewModel.mOriginalNoteTitle = mNoteCursor.getString(mNoteTitlePos);
        mViewModel.mOriginalNoteText = mNoteCursor.getString(mNoteTextPos);
        mViewModel.mNoteUri = mNoteUri.toString();
    }

    private void displayNote() {
        boolean isNotEmpty = mNoteCursor.moveToFirst();
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourseId(courseId);

        mSpinnerCoursers.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);

        CourseEventBroadcastHelper.sendBroadcast(this, courseId, "Editing Note");
    }

    public int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;
        boolean more = cursor.moveToFirst();

        while (more == true) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if (cursorCourseId.equals(courseId))
                break;
            courseRowIndex++;
            more = cursor.moveToNext();
        }

        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        } else {
            mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        }
    }

    private void createNewNote() {
        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... params) {
                Log.d(TAG, "doInBackgroung - thread: " + Thread.currentThread().getId());
                ContentValues insertValues = params[0];
                Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);
                simulateLongRunningWork();
                publishProgress(2);

                simulateLongRunningWork();
                publishProgress(3);

                return rowUri;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                Log.d(TAG, "onPostExecute - thread: " + Thread.currentThread().getId());
                mNoteUri = uri;
                displaySnackbar(mNoteUri.toString());
                mProgressBar.setVisibility(View.GONE);
            }
        };

        final ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        Log.d(TAG, "Call to execute - thread: " + Thread.currentThread().getId());
        task.execute(values);

//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
//                mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
//                return null;
//            }
//        };
//        task.execute();
    }

    private void simulateLongRunningWork() {
        try {
            Thread.sleep(2000);
        } catch(Exception ex) {}
    }

    private void displaySnackbar(String message) {
        View view = findViewById(R.id.spinner_courses);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        } else if (id == R.id.action_next) {
            moveNext();
        } else if (id == R.id.action_set_reminder) {
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        int noteId = (int) ContentUris.parseId(mNoteUri);

        Intent intent = new Intent(this, NoteReminderReceiver.class);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long currentTimeMilliseconds = SystemClock.elapsedRealtime();
        long ONE_HOUR = 60 * 60 * 1000;
        long TEN_SECONDS = 10 * 1000;

        long alarmTime = currentTimeMilliseconds + TEN_SECONDS;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNodeIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNodeIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            if (mIsNewNote) {
                deleteNoteFromDatabase();
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
    }

    private void deleteNoteFromDatabase() {
//        final String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? ";
//        final String[] selectionArgs = {Integer.toString(mNoteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
//                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
//                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);

                getContentResolver().delete(mNoteUri, null, null);
                return null;
            }
        };
        task.execute();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null)
            mViewModel.saveState(outState);
    }

    private void storePreviousNoteValues() {
        saveNoteToDatabase(mViewModel.mOriginalNoteCourseId, mViewModel.mOriginalNoteTitle, mViewModel.mOriginalNoteText);

//        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
//        mNote.setCourse(course);
//        mNote.setTitle(mViewModel.mOriginalNoteTitle);
//        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCoursers.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private String selectedCourseTitle() {
        int selectedPosition = mSpinnerCoursers.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseTitlePos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        String courseTitle = cursor.getString(courseTitlePos);
        return courseTitle;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
//        final String selection = NoteInfoEntry._ID + " = ?";
//        final String[] selectionArgs = {Integer.toString(mNoteId)};

        final ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
//                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
//                db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);

                getContentResolver().update(mNoteUri, values, null, null);
                return null;
            }
        };
        task.execute();
    }

    private void sendEmail() {
//        CourseInfo course = (CourseInfo) mSpinnerCoursers.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                selectedCourseTitle() + "\"\n" + mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfr2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };

        return new CursorLoader(this, uri, courseColumns, null,
                null, Courses.COLUMN_COURSE_TITLE);

//        return new CursorLoader(this) {
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
//                String[] courseColumns = {
//                        CourseInfoEntry.COLUMN_COURSE_TITLE,
//                        CourseInfoEntry.COLUMN_COURSE_ID,
//                        CourseInfoEntry._ID
//                };
//                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
//                        null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
//            }
//        };
    }

    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;
        String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);

        return new CursorLoader(this, mNoteUri, noteColumns,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_NOTES:
                loadFinishedNotes(data);
                break;
            case LOADER_COURSES:
                mAdapterCourses.changeCursor(data);
                mCoursesQueryFinished = true;
                displayNoteWhenQueriesFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        mNotesQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if (mNotesQueryFinished && mCoursesQueryFinished) {
            displayNote();
            saveOriginalNoteValues();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_NOTES:
                if (mNoteCursor != null)
                    mNoteCursor.close();
                break;
            case LOADER_COURSES:
                mAdapterCourses.changeCursor(null);
        }
    }
}
