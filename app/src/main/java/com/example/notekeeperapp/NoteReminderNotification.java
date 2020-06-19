package com.example.notekeeperapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.example.notekeeperapp.MainActivity;
import com.example.notekeeperapp.NoteActivity;
import com.example.notekeeperapp.NoteBackup;
import com.example.notekeeperapp.NoteBackupService;
import com.example.notekeeperapp.NotificationUtil;
import com.example.notekeeperapp.R;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NoteReminderNotification {
    private static final int NOTIFICATION_ID = 888;
    public static final String NOTES_CHANNEL_ID = "notes_channel_id";
    public static final String NOTES_CHANNEL_NAME = "Notes";

    public static void notify(Context context, String noteTitle, String noteText, int noteId) {
        String notificationChannelId = NotificationUtil.createNotificationChannel(context,
                NOTES_CHANNEL_ID, NOTES_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationChannelId);
        final Bitmap picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);

        Intent noteActivityIntent = new Intent(context, NoteActivity.class);
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, noteId);

        Intent backupServiceIntent = new Intent(context, NoteBackupService.class);
        backupServiceIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

        Notification notification = builder
                // Set appropriate defaults for the notification light, sound and vibration
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Review note")
                .setContentText(noteText)
                .setSmallIcon(R.drawable.ic_assignment_late_black_24dp)
                .setLargeIcon(picture)
                .setChannelId(NOTES_CHANNEL_ID)
                .setTicker("Review note")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(noteText)
                        .setBigContentTitle(noteTitle)
                        .setSummaryText("Review note"))
                .setContentIntent(PendingIntent.getActivity(
                        context,
                        0,
                        noteActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        0,
                        "View all notes",
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        0,
                        "Backup notes",
                        PendingIntent.getService(
                                context,
                                0,
                                backupServiceIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
                // Automatically dismiss the notification when it is touched
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification);
    }
}
