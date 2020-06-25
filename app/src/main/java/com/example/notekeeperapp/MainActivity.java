package com.example.notekeeperapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final int LOADER_NOTES = 0;
    public static final int NOTE_UPLOAD_JOB_ID = 1;
    public static final String USER_DISPLAY_NAME = "user_display_name";
    public static final String USER_EMAIL_ADDRESS = "user_email_address";
    private final String TAG = getClass().getSimpleName();
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private AppBarConfiguration mAppBarConfiguration;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCoursesLayoutManager;
    private MainActivityViewModel mMainViewModel;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "********************** onCreate **********************");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableStrictMode();

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mMainViewModel = viewModelProvider.get(MainActivityViewModel.class);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        // this didn't seem necessary
//        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        mDrawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_notes, R.id.nav_courses)
                .setDrawerLayout(mDrawer)
                .build();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);

        // without this it wasn't selecting the options on the navigation drawer
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class ));
            return true;
        } else if (id == R.id.action_backup_notes) {
            backupNotes();
        } else if (id == R.id.action_upload_notes) {
            scheduleNoteUpload();
        }

        return super.onOptionsItemSelected(item);
    }

    private void scheduleNoteUpload() {
//        PersistableBundle extras = new PersistableBundle();
//        extras.putString(NoteUploaderJobService.EXTRA_DATA_URI, Notes.CONTENT_URI.toString());
//
//        ComponentName componentName = new ComponentName(this, NoteUploaderJobService.class);
//        JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOAD_JOB_ID, componentName)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                .setExtras(extras)
//                .build();
//
//        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
//        jobScheduler.schedule(jobInfo);
    }

    private void backupNotes() {
        Intent intent = new Intent(this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateNavHeader();

//        openDrawer();
    }

    private void openDrawer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        }, 1000);
    }

    private void updateNavHeader() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        final TextView textUserName = (TextView) headerView.findViewById(R.id.text_user_name);
        final TextView textEmailAddress = (TextView) headerView.findViewById(R.id.text_email_address);

        AsyncTask<Context, Void, ContentValues> task = new AsyncTask<Context, Void, ContentValues>() {
            @Override
            protected ContentValues doInBackground(Context... contexts) {
                Context context = contexts[0];
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                String userName = pref.getString(USER_DISPLAY_NAME, "");
                String emailAddress = pref.getString(USER_EMAIL_ADDRESS, "");

                ContentValues values = new ContentValues();
                values.put(USER_DISPLAY_NAME, userName);
                values.put(USER_EMAIL_ADDRESS, emailAddress);
                return values;
            }

            @Override
            protected void onPostExecute(ContentValues contentValues) {
                textUserName.setText(contentValues.getAsString(USER_DISPLAY_NAME));
                textEmailAddress.setText(contentValues.getAsString(USER_EMAIL_ADDRESS));
            }
        };

        task.execute(this);
    }

    private void initializeDisplayContent() {
        mRecyclerItems = findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mCoursesLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.course_grid_span));

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, null);

        displayNotes();
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
//        menu.findItem(id).setChecked(true);
        navigationView.setCheckedItem(menu.findItem(id));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_courses) {
            displayCourses();
        } else if (id == R.id.nav_share) {
//            handleSelection(R.string.nav_share_message);
            handleShare();
        } else if (id == R.id.nav_send) {
            handleSelection(R.string.nav_send_message);
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);

        mMainViewModel.getNotesExpanded().observe(this, new Observer<List<NoteInfoExpanded>>() {
            @Override
            public void onChanged(List<NoteInfoExpanded> notesInfoExpanded) {
                mNoteRecyclerAdapter.changeNotes(notesInfoExpanded);
                mRecyclerItems.setAdapter(mNoteRecyclerAdapter);
            }
        });

        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void displayCourses() {
        mRecyclerItems.setLayoutManager(mCoursesLayoutManager);

        mMainViewModel.getCourses().observe(this, new Observer<List<CourseInfo>>() {
            @Override
            public void onChanged(List<CourseInfo> courses) {
                mCourseRecyclerAdapter.changeCoutses(courses);
                mRecyclerItems.setAdapter(mCourseRecyclerAdapter);
            }
        });

        selectNavigationMenuItem(R.id.nav_courses);
    }

    private void handleShare() {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, "Share to - " +
                PreferenceManager.getDefaultSharedPreferences(this).getString("user_favorite_social", ""),
                Snackbar.LENGTH_LONG).show();
    }

    private void handleSelection(int message_id) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, message_id, Snackbar.LENGTH_LONG).show();
    }
}
