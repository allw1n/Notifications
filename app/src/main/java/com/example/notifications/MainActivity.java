package com.example.notifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.notifications.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    public static final String PRIMARY_CHANNEL_ID = "PRIMARY_NOTIFICATIONS_CHANNEL";
    public static final String PRIMARY_CHANNEL_NAME = "Mascot Notification";

    public static final String TAPPED = "TAPPED";
    public static final String NOTIFIED = "NOTIFIED";
    public static final String BROADCAST_UPDATED = "BROADCAST_UPDATED";
    public static final String ACTIVITY_UPDATED = "ACTIVITY_UPDATED";
    public static final String TAPPED_PREFS = "TAPPED_PREFS";

    public static final String ACTION_UPDATE_NOTIFICATION =
            BuildConfig.APPLICATION_ID + ".ACTION_UPDATE_NOTIFICATION";
    private final UpdateNotificationReceiver updateReceiver = new UpdateNotificationReceiver();

    public static final String ACTION_CANCEL_NOTIFICATION =
            BuildConfig.APPLICATION_ID + ".ACTION_CANCEL_NOTIFICATION";
    private final CancelNotificationReceiver cancelReceiver = new CancelNotificationReceiver();

    public static final int NOTIFICATION_ID = 0;

    private SharedPreferences tappedPrefs;
    private SharedPreferences.Editor tappedEditor;

    private MaterialButton buttonNotify, buttonUpdate, buttonCancel;
    private NotificationManager notificationManager;
    
    private Bitmap androidImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        buttonNotify = binding.buttonNotify;
        buttonUpdate = binding.buttonUpdate;
        buttonCancel = binding.buttonCancel;
        
        androidImage = BitmapFactory.decodeResource(getResources(), R.drawable.mascot_1);

        tappedPrefs = getSharedPreferences(TAPPED_PREFS, MODE_PRIVATE);
        tappedEditor = tappedPrefs.edit();
        Log.d("onCreate", tappedPrefs.getString(TAPPED, "created"));
        tappedEditor.clear();
        tappedEditor.apply();

        this.registerReceiver(updateReceiver, new IntentFilter(ACTION_UPDATE_NOTIFICATION));
        this.registerReceiver(cancelReceiver, new IntentFilter(ACTION_CANCEL_NOTIFICATION));

        buttonNotify.setOnClickListener(view -> sendNotification());

        buttonUpdate.setOnClickListener(view -> updateNotification());

        buttonCancel.setOnClickListener(view -> cancelNotification());

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = new NotificationChannel(
                PRIMARY_CHANNEL_ID,
                PRIMARY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("Notification from Mascot");
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public void sendNotification() {

        Log.d("sendNotification", "called");

        tappedEditor.putString(TAPPED, NOTIFIED);
        tappedEditor.apply();

        Intent broadcastUpdateIntent = new Intent(ACTION_UPDATE_NOTIFICATION);

        PendingIntent broadcastUpdatePendingIntent = PendingIntent.getBroadcast(this,
                NOTIFICATION_ID,
                broadcastUpdateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Intent activityUpdateIntent = new Intent(this, MainActivity.class);
        activityUpdateIntent.putExtra(TAPPED, NOTIFIED);

        PendingIntent activityUpdatePendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID,
                activityUpdateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //Creating default expandable notification
        NotificationCompat.Builder builder = getNotificationBuilder();
        builder.setContentTitle("You've been notified")
                .setContentText("Notification text.")
                .setLargeIcon(androidImage)
                .setStyle(
                        new NotificationCompat.BigPictureStyle()
                        .bigPicture(androidImage)
                        .bigLargeIcon(null))
                .setContentIntent(activityUpdatePendingIntent)
                .addAction(R.drawable.ic_update, "UPDATE", broadcastUpdatePendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        toggleButtons(false, true, true);
    }

    public void updateNotification() {

        Log.d("updateNotification", "called");

        Intent cancelIntent = new Intent(ACTION_CANCEL_NOTIFICATION);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this,
                NOTIFICATION_ID,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        ytIntent.putExtra(TAPPED, ACTIVITY_UPDATED);
        PendingIntent ytPendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID,
                ytIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews expandedNotificationLayout = new RemoteViews(getPackageName(), R.layout.custom_expanded_notification);
        RemoteViews collapsedNotificationLayout = new RemoteViews(getPackageName(), R.layout.custom_collapsed_notification);

        expandedNotificationLayout.setOnClickPendingIntent(R.id.view_open_link, cancelPendingIntent);

        NotificationCompat.Builder builder = getNotificationBuilder();
        builder.setContentIntent(ytPendingIntent)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(collapsedNotificationLayout)
                .setCustomBigContentView(expandedNotificationLayout);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        toggleButtons(false, false, true);
    }

    public void cancelNotification() {

        Log.d("cancelNotification", "called");

        notificationManager.cancel(NOTIFICATION_ID);
        toggleButtons(true, false, false);
    }

    private NotificationCompat.Builder getNotificationBuilder() {

        return new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_android)
                .setLargeIcon(androidImage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
    }

    private void toggleButtons(boolean notifyEnabled, boolean updateEnabled, boolean cancelEnabled) {
        buttonNotify.setEnabled(notifyEnabled);
        buttonUpdate.setEnabled(updateEnabled);
        buttonCancel.setEnabled(cancelEnabled);
    }

    public class UpdateNotificationReceiver extends BroadcastReceiver {
        public UpdateNotificationReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            tappedEditor.putString(TAPPED_PREFS, BROADCAST_UPDATED);
            tappedEditor.apply();
            updateNotification();
        }
    }

    public class CancelNotificationReceiver extends BroadcastReceiver {
        public CancelNotificationReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            cancelNotification();
            Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
            startActivity(ytIntent);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(updateReceiver);
        unregisterReceiver(cancelReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (tappedPrefs.getString(TAPPED, "").equals(BROADCAST_UPDATED)) {

            Log.d("onResume", tappedPrefs.getString(TAPPED, ""));
            toggleButtons(true, false, false);
            tappedEditor.clear();
            tappedEditor.apply();
        }
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getStringExtra(TAPPED).equals(NOTIFIED)) {

            Log.d("Notified intent extra", intent.getStringExtra(TAPPED));
            tappedEditor.putString(TAPPED_PREFS, ACTIVITY_UPDATED);
            tappedEditor.apply();
            updateNotification();
        }
        super.onNewIntent(intent);
    }
}