package com.dts.roadp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class srvBase extends android.app.Service {

    public  String URL="",error="";

    private NotificationManager notificationManager;

    private boolean idle=false;
    private String appname="Road";
    private int iconresource=R.drawable.logo_panel;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (initSession(intent)) execute();
        return android.app.Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {}

    //region Main

    public void execute() {}

    public void loadParams(Intent intent) {}

    private boolean initSession(Intent intent) {
        try {
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            URL = intent.getStringExtra("URL");

            loadParams(intent);

            return true;
        } catch (Exception e) {
            //notification("Error inicio : "+e.getMessage());
            return false;
        }
    }

    //endregion

    //region Notification

    public void notification(String message) {

        int notificationId = createID();
        String channelId = "channel-id";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(iconresource)
                .setContentTitle(appname)
                .setContentText(message)
                .setVibrate(new long[]{100, 250})
                .setLights(Color.YELLOW, 500, 5000)
                .setTimeoutAfter(30000)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#6200EE"));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, srvBase.class));
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());

        removeNotification(notificationId);
    }

    private void removeNotification(int id) {
        final int notifid=id;

        Handler handler = new Handler();
        long delayInMilliseconds = 30000;
        handler.postDelayed(new Runnable() {
            public void run() {
                notificationManager.cancel(notifid);
            }
        }, delayInMilliseconds);
    }

    private int createID() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", java.util.Locale.ENGLISH).format(now));
        return id;
    }

    //endregion

}
