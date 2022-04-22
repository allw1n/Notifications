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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.notifications.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String PRIMARY_CHANNEL_ID = "PRIMARY_NOTIFICATIONS_CHANNEL";
    private static final String PRIMARY_CHANNEL_NAME = "Mascot Notification";
    private static final String TAG_TAPPED_OR_CLEARED = "TAG_TAPPED_OR_CLEARED";
    private static final String TAPPED = "TAPPED";
    private static final String CLEARED = "CLEARED";

    private static final String ACTION_UPDATE_NOTIFICATION =
            BuildConfig.APPLICATION_ID + ".ACTION_UPDATE_NOTIFICATION";
    private final NotificationReceiver receiver = new NotificationReceiver();

    private static final int NOTIFICATION_ID = 0;
    private MaterialButton buttonNotify, buttonUpdate, buttonCancel;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        buttonNotify = binding.buttonNotify;
        buttonUpdate = binding.buttonUpdate;
        buttonCancel = binding.buttonCancel;

        /*Intent pendingIntent = getIntent();
        if (pendingIntent != null) {
            if (pendingIntent.hasExtra(TAG_TAPPED_OR_CLEARED)) {
                Log.d("Pending Intent", "Has Extra");

                if (pendingIntent.getStringExtra(TAG_TAPPED_OR_CLEARED).equals(TAPPED)) {
                    Log.d("Pending tapped check", pendingIntent.getStringExtra(TAG_TAPPED_OR_CLEARED));

                } else if (pendingIntent.getStringExtra(TAG_TAPPED_OR_CLEARED).equals(CLEARED)){
                    Log.d("Pending cleared check", pendingIntent.getStringExtra(TAG_TAPPED_OR_CLEARED));
                    //toggleButtons(true, false, false);

                } else {
                    Log.d("Pending else check", pendingIntent.getStringExtra(TAG_TAPPED_OR_CLEARED));
                }
            }
        }*/

        this.registerReceiver(receiver, new IntentFilter(ACTION_UPDATE_NOTIFICATION));

        buttonNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification();
            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateNotification();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelNotification();
            }
        });

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

    private void sendNotification() {

        Intent updateIntent = new Intent(ACTION_UPDATE_NOTIFICATION);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(this,
                NOTIFICATION_ID,
                updateIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap androidImage = BitmapFactory.decodeResource(getResources(), R.drawable.mascot_1);

        //Creating default expandable notification
        NotificationCompat.Builder builder = getNotificationBuilder();
        builder.setLargeIcon(androidImage)
                .setStyle(
                        new NotificationCompat.BigPictureStyle()
                        .bigPicture(androidImage)
                        .bigLargeIcon(null))
                .addAction(R.drawable.ic_update, "UPDATE NOTIFICATION", updatePendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        toggleButtons(false, true, true);
    }

    private void updateNotification() {
        Bitmap androidImage = BitmapFactory.decodeResource(getResources(), R.drawable.mascot_1);

        //Updating to a custom expandable notification
        NotificationCompat.Builder builder = getNotificationBuilder();

        RemoteViews defaultNotificationLayout = new RemoteViews(getPackageName(), R.layout.custom_default_notification);
        RemoteViews expandedNotificationLayout = new RemoteViews(getPackageName(), R.layout.custom_default_notification);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        toggleButtons(false, false, true);
    }

    private void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
        toggleButtons(true, false, false);
    }

    private NotificationCompat.Builder getNotificationBuilder() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(TAG_TAPPED_OR_CLEARED, TAPPED);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        /*Intent clearedIntent = new Intent(this, MainActivity.class);
        clearedIntent.putExtra(TAG_TAPPED_OR_CLEARED, CLEARED);
        PendingIntent pendingClearedIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID,
                clearedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);*/

        return new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_android)
                .setContentTitle("You've been notified")
                .setContentText("Notification text.")
                .setAutoCancel(true)
                .setContentIntent(pendingNotificationIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
    }

    private void toggleButtons(boolean notifyEnabled, boolean updateEnabled, boolean cancelEnabled) {
        buttonNotify.setEnabled(notifyEnabled);
        buttonUpdate.setEnabled(updateEnabled);
        buttonCancel.setEnabled(cancelEnabled);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);

        super.onDestroy();
    }

    public class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotification();
        }

    }
}