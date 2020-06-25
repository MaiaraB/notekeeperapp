package com.example.notekeeperapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_ID = "com.example.notekeeperapp.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int NOTIFICATION_ID = 888;
    private final String TAG = getClass().getSimpleName();
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCoursers;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private Long mNoteId;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;
    private ArrayAdapter<CourseInfo> mAdapterCourses;
    private Uri mNoteUri;
    private ModuleStatusView mViewModuleStatus;
    private NoteKeeperAppDatabase mDb;
    private List<ModuleInfo> mCourseModules;
    private long mSelectedCourseId;
    //    private NotificationManagerCompat mNotificationManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        mNotificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        mDb = NoteKeeperAppDatabase.getInstance(getApplicationContext());

        readDisplayStateValues();

        NoteActivityViewModelFactory noteActivityViewModelFactory = new NoteActivityViewModelFactory(mDb, mNoteId);
        mViewModel = new ViewModelProvider(this, noteActivityViewModelFactory).get(NoteActivityViewModel.class);

        if (mViewModel.isNewlyCreated && savedInstanceState != null) {
            mViewModel.restoreState(savedInstanceState);
            String stringNoteUri = savedInstanceState.getString(NoteActivityViewModel.NOTE_URI);
            mNoteUri = Uri.parse(stringNoteUri);
        }

        mViewModel.isNewlyCreated = false;

        mSpinnerCoursers = findViewById(R.id.spinner_courses);
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);
        mViewModuleStatus = (ModuleStatusView) findViewById(R.id.module_status);
        mViewModuleStatus.setModuleStatus(new boolean[1]);

        mViewModel.fetchSpinnerCourses().observe(this, new Observer<List<CourseInfo>>() {
            @Override
            public void onChanged(List<CourseInfo> courses) {
                mAdapterCourses = new ArrayAdapter<CourseInfo>(NoteActivity.this, android.R.layout.simple_spinner_item, courses);
                mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinnerCoursers.setAdapter(mAdapterCourses);

                if (!mIsNewNote) {
                    mViewModel.getNote().observe(NoteActivity.this, new Observer<NoteInfo>() {
                        @Override
                        public void onChanged(NoteInfo noteInfo) {
                            mNote = noteInfo;
                            displayNote();

                            mSelectedCourseId = noteInfo.getCourseId();
                            mViewModel.getNoteCourseModules(mSelectedCourseId).observe(NoteActivity.this, new Observer<CourseWithModules>() {
                                @Override
                                public void onChanged(CourseWithModules courseWithModules) {
                                    mCourseModules = courseWithModules.mModules;
                                    loadModuleStatusValues();
                                    saveOriginalNoteValues();
                                }
                            });
                        }
                    });
                }
            }
        });


    }

    private void loadModuleStatusValues() {
        boolean[] moduleStatus = new boolean[mCourseModules.size()];

        for (int moduleIndex = 0; moduleIndex < mCourseModules.size(); moduleIndex++)
            moduleStatus[moduleIndex] = mCourseModules.get(moduleIndex).isComplete();

        mViewModuleStatus.setModuleStatus(moduleStatus);
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;

        mViewModel.mOriginalNoteCourseId = mNote.getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();
        boolean[] moduleStatuses = mViewModuleStatus.getModuleStatus();
        mViewModel.mModuleStatuses = Arrays.copyOf(moduleStatuses, moduleStatuses.length);
    }

    private void displayNote() {
        long courseId = mNote.getCourseId();
        String noteTitle = mNote.getTitle();
        String noteText = mNote.getText();

        int courseIndex = getIndexOfCourseId(courseId);

        mSpinnerCoursers.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);

        CourseEventBroadcastHelper.sendBroadcast(this, getStringCourseId(courseId), "Editing Note");
    }

    public int getIndexOfCourseId(long courseId) {
        int courseIndex = 0;

        for (; courseIndex < mAdapterCourses.getCount(); courseIndex++) {
            if (mAdapterCourses.getItem(courseIndex).getId() == courseId)
                break;
        }

        return courseIndex;
    }

    public String getStringCourseId(long courseId) {
        String courseIdString = "";

        for (int i = 0; i < mAdapterCourses.getCount(); i++) {
            if (mAdapterCourses.getItem(i).getId() == courseId) {
                courseIdString = mAdapterCourses.getItem(i).getCourseId();
            }
        }

        return courseIdString;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getLongExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }
    }

    private void createNewNote() {
        AsyncTask<Void, Integer, Long> task = new AsyncTask<Void, Integer, Long>() {
            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected Long doInBackground(Void... voids) {
                Log.d(TAG, "doInBackgroung - thread: " + Thread.currentThread().getId());
                long noteId = mViewModel.createNote(mNote);
                simulateLongRunningWork();
                publishProgress(2);

                simulateLongRunningWork();
                publishProgress(3);

                return noteId;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Long noteId) {
                Log.d(TAG, "onPostExecute - thread: " + Thread.currentThread().getId());
                mNoteId = noteId;
                mNote.setId(mNoteId);
                displaySnackbar("Note id: " + mNoteId);
                mProgressBar.setVisibility(View.GONE);
            }
        };

        mNote = new NoteInfo(-1, "", "");

        Log.d(TAG, "Call to execute - thread: " + Thread.currentThread().getId());
        task.execute();

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

        Intent intent = new Intent(this, NoteReminderReceiver.class);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, mNoteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long currentTimeMilliseconds = SystemClock.elapsedRealtime();
        long ONE_HOUR = 60 * 60 * 1000;
        long TEN_SECONDS = 10 * 1000;

        long alarmTime = currentTimeMilliseconds + TEN_SECONDS;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mViewModel.getNotesCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer notesCount) {
                MenuItem item = menu.findItem(R.id.action_next);
                int lastNodeIndex = notesCount - 1;
                item.setEnabled(mNoteId < lastNodeIndex);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;
        mViewModel.setNoteId(mNoteId).observe(this, new Observer<NoteInfo>() {
            @Override
            public void onChanged(NoteInfo noteInfo) {
                mNote = noteInfo;
//                saveOriginalNoteValues();
                displayNote();
                invalidateOptionsMenu();
            }
        });
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
        mViewModel.deleteNote(mNote);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null)
            mViewModel.saveState(outState);
    }

    private void storePreviousNoteValues() {
        saveNoteToDatabase(mViewModel.mOriginalNoteCourseId, mViewModel.mOriginalNoteTitle, mViewModel.mOriginalNoteText, mViewModel.mModuleStatuses);
    }

    private void saveNote() {
        long courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        boolean[] moduleStatuses = mViewModuleStatus.getModuleStatus();
        saveNoteToDatabase(courseId, noteTitle, noteText, moduleStatuses);
    }

    private long selectedCourseId() {
        int selectedPosition = mSpinnerCoursers.getSelectedItemPosition();
        CourseInfo course = mAdapterCourses.getItem(selectedPosition);

        return course.getId();
    }

    private String selectedCourseTitle() {
        int selectedPosition = mSpinnerCoursers.getSelectedItemPosition();
        CourseInfo course = mAdapterCourses.getItem(selectedPosition);

        return course.getTitle();
    }

    private void saveNoteToDatabase(long courseId, String noteTitle, String noteText, boolean[] moduleStatuses) {
        mViewModel.updateNote(new NoteInfo(mNoteId, courseId, noteTitle, noteText));

        for (int i = 0; i < moduleStatuses.length; i++)
            mCourseModules.get(i).setComplete(moduleStatuses[i]);
        mViewModel.updateModules(mCourseModules.toArray(new ModuleInfo[mCourseModules.size()]));
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
}
