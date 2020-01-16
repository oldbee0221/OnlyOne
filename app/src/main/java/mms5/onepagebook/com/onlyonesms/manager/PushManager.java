package mms5.onepagebook.com.onlyonesms.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

import mms5.onepagebook.com.onlyonesms.LogInActivity;
import mms5.onepagebook.com.onlyonesms.R;

public class PushManager {
    public static final String CHANNEL_SERVICE_ID = "mms5.onepagebook.com.onlyonesms.service.TaskHandlerService";
    public static final String CHANNEL_SERVICE_CHANNEL = "mms5.onepagebook.com.onlyonesms.service.TaskHandlerService.Channel";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelService = new NotificationChannel(CHANNEL_SERVICE_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channelService.setDescription(context.getString(R.string.app_name));
            channelService.setLightColor(Color.BLUE);
            channelService.setShowBadge(false);
            channelService.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager(context).createNotificationChannel(channelService);

            NotificationChannel channelMessage = new NotificationChannel(context.getString(R.string.default_notification_channel_id),
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channelMessage.setDescription(context.getString(R.string.notification_channel_alarm_description));
            channelMessage.setLightColor(Color.BLUE);
            channelMessage.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager(context).createNotificationChannel(channelMessage);
        }
    }

    public static void sendNotification(Context context, String title, String body) {
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context, context.getString(R.string.default_notification_channel_id))
                    .setContentIntent(makeIntent(context))
                    .setSmallIcon(R.drawable.ic_app_icon)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_app_icon))
                    .setAutoCancel(true)
                    .setVibrate(new long[]{1000, 1000})
                    .build();
        } else {
            notification = new NotificationCompat.Builder(context)
                    .setContentIntent(makeIntent(context))
                    .setSmallIcon(R.drawable.ic_app_icon)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_app_icon))
                    .setAutoCancel(true)
                    .setVibrate(new long[]{1000, 1000})
                    .build();
        }

        int id = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
        getManager(context).notify(id, notification);
    }

    private static PendingIntent makeIntent(Context context) {
        Intent intent = new Intent(context, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static NotificationManager getManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void clearAll(Context context) {
        getManager(context).cancelAll();
    }
}
