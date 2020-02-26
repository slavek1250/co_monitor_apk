package com.kala.co.co_monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.app.NotificationManager


class CoNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val handled = intent?.getBooleanExtra("setNotificationAsHandled", false)
        val eventId = intent?.getIntExtra("eventId", 0)

        if(handled!! && CoNotificationData.getEventId() == eventId) {
            CoNotificationData.setAsHandled()
            Toast.makeText(context, "Ok, nie ponowię powidomienia.", Toast.LENGTH_SHORT).show()

            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(eventId)
        }
    }
}