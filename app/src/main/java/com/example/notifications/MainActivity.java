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
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.notifications.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    public static final String PRIMARY_CHANNEL_ID = "PRIMARY_NOTIFICATIONS_CHANNEL";
    public static final String PRIMARY_CHANNEL_NAME = "Mascot Notification";

    public static final String ACTION_SEND_NOTIFICATION =
            BuildConfig.APPLICATION_ID + ".ACTION_SEND_NOTIFICATION";
    private final UpdateNotificationReceiver updateReceiver = new UpdateNotificationReceiver();

    public static final String ACTION_CANCEL_NOTIFICATION =
            BuildConfig.APPLICATION_ID + ".ACTION_CANCEL_NOTIFICATION";

    public static final int NOTIFICATION_ID = 0;
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

        this.registerReceiver(updateReceiver, new IntentFilter(ACTION_SEND_NOTIFICATION));

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

    public void sendNotification() {

        Intent updateIntent = new Intent(ACTION_SEND_NOTIFICATION);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(this,
                NOTIFICATION_ID,
                updateIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        //Creating default expandable notification
        NotificationCompat.Builder builder = getNotificationBuilder();
        builder.setContentTitle("You've been notified")
                .setContentText("Notification text.")
                .setLargeIcon(androidImage)
                .setStyle(
                        new NotificationCompat.BigPictureStyle()
                        .bigPicture(androidImage)
                        .bigLargeIcon(null))
                .addAction(R.drawable.ic_update, "Update", updatePendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        toggleButtons(false, true, true);
    }

    public void updateNotification() {

        Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        PendingIntent pendingYTIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID,
                ytIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews expandedNotificationLayout = new RemoteViews(getPackageName(), R.layout.custom_expanded_notification);
        RemoteViews collapsedNotificationLayout = new RemoteViews(getPackageName(), R.layout.custom_collapsed_notification);

        expandedNotificationLayout.setOnClickPendingIntent(R.id.view_open_link, pendingYTIntent);
        collapsedNotificationLayout.setOnClickPendingIntent(R.id.view_open_link, pendingYTIntent);

        NotificationCompat.Builder builder = getNotificationBuilder();
        builder.setContentIntent(pendingYTIntent)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(collapsedNotificationLayout)
                .setCustomBigContentView(expandedNotificationLayout);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        toggleButtons(false, false, true);
    }

    public void cancelNotification() {
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(updateReceiver);

        super.onDestroy();
    }

    public class UpdateNotificationReceiver extends BroadcastReceiver {
        public UpdateNotificationReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotification();
        }
    }
}