package com.example.notekeeperapp;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {NoteInfo.class, CourseInfo.class, ModuleInfo.class}, views = {NoteInfoExpanded.class},version = 1)
public abstract class NoteKeeperAppDatabase extends RoomDatabase {

    private static NoteKeeperAppDatabase mInstance;

    public abstract NoteDao noteDao();
    public abstract CourseDao courseDao();
    public abstract ModuleDao moduleDao();

    public static synchronized NoteKeeperAppDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = Room.databaseBuilder(context.getApplicationContext(),
                    NoteKeeperAppDatabase.class, "notekeeper.db")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }

        return mInstance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(mInstance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private NoteDao mNoteDao;
        private CourseDao mCourseDao;
        private ModuleDao mModuleDao;

        private PopulateDbAsyncTask(NoteKeeperAppDatabase db) {
            mNoteDao = db.noteDao();
            mCourseDao = db.courseDao();
            mModuleDao = db.moduleDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mCourseDao.insertCourse(new CourseInfo("android_intents", "Android Programming with Intents"));
            mCourseDao.insertCourse(new CourseInfo("android_async", "Android Async Programming and Services"));
            mCourseDao.insertCourse(new CourseInfo("java_lang", "Java Fundamentals: The Java Language"));
            mCourseDao.insertCourse(new CourseInfo("java_core", "Java Fundamentals: The Core Platform"));

            mNoteDao.insertNote(new NoteInfo(1, "Dynamic intent resolution", "Wow, intents allow components to be resolved at runtime"));
            mNoteDao.insertNote(new NoteInfo(1, "Delegating intents", "PendingIntents are powerful; they delegate much more than just a component invocation"));
            mNoteDao.insertNote(new NoteInfo(2, "Service default threads", "Did you know that by default an Android Service will tie up the UI thread?"));
            mNoteDao.insertNote(new NoteInfo(2, "Long running operations", "Foreground Services can be tied to a notification icon"));
            mNoteDao.insertNote(new NoteInfo(3, "Parameters", "Leverage variable-length parameter lists?"));
            mNoteDao.insertNote(new NoteInfo(3, "Anonymous classes", "Anonymous classes simplify implementing one-use types"));
            mNoteDao.insertNote(new NoteInfo(4, "Compiler options", "The -jar option isn't compatible with with the -cp option"));
            mNoteDao.insertNote(new NoteInfo(4, "Serialization", "Remember to include SerialVersionUID to assure version compatibility"));

            mModuleDao.insertModule(new ModuleInfo(1, "create_intents", "How to create intents", true));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras", "How to add extras to the intent"));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras2", "How to add extras to the intent 2", true));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras3", "How to add extras to the intent 3", true));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras4", "How to add extras to the intent 4"));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras5", "How to add extras to the intent 5"));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras6", "How to add extras to the intent 6", true));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras7", "How to add extras to the intent 7"));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras8", "How to add extras to the intent 8"));
            mModuleDao.insertModule(new ModuleInfo(1, "intents_extras9", "How to add extras to the intent 9", true));
            mModuleDao.insertModule(new ModuleInfo(2, "async_task", "How to create AsyncTask"));
            mModuleDao.insertModule(new ModuleInfo(2, "threads", "AsyncTask helping with Threads"));
            mModuleDao.insertModule(new ModuleInfo(3, "syntax_java", "Tha basics of java syntax"));
            mModuleDao.insertModule(new ModuleInfo(3, "compiler_java", "Understanding java compilation"));
            mModuleDao.insertModule(new ModuleInfo(4, "java_class", "What are java classes"));
            mModuleDao.insertModule(new ModuleInfo(4, "java_interface", "Creating Java interfaces"));



            return null;
        }
    }
}
