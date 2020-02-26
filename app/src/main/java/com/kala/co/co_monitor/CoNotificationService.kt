package com.kala.co.co_monitor

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import kotlin.concurrent.timer

class CoNotificationService : Service() {

    private var notificationManager: NotificationManagerCompat? = null

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        CoNotificationData.setApiResponseCallback {
            apiResponseCallback()
        }
        timer(
            "ok",
            false,
            1000,
            10000,
            {
                CoNotificationData.fetchEvents()
            })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun apiResponseCallback() {
        if(!CoNotificationData.hasAnEventOccurred() || CoNotificationData.isEventHandled()) {
            return
        }

        val activityIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this,
            0, activityIntent, 0)

        val broadcastIntent = Intent(this, CoNotificationReceiver::class.java)
        broadcastIntent.putExtra("setNotificationAsHandled", true)
        broadcastIntent.putExtra("eventId", CoNotificationData.getEventId())
        val actionIntent = PendingIntent.getBroadcast(this,
            0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, CoNotificationChannel.CHANNEL_1_ID)
            .setSmallIcon(R.drawable.ic_one)
            .setContentTitle(CoNotificationData.getEventDescription())
            .setContentText(CoNotificationData.getOperationDescription())
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_home_red, "OK", actionIntent)
            .build()

        notificationManager?.notify(CoNotificationData.getEventId(), notification)
    }
}