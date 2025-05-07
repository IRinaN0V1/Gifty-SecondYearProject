package com.example.gifty

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class EventNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Получаем детали события из интента
        val eventTitle = intent.getStringExtra("event_title")
        val eventDate = intent.getStringExtra("event_date")
        // Проверяем разрешение на уведомления
        if (!areNotificationsEnabled(context)) {
            requestNotificationPermission(context)
            return
        }

        // Создаем Notification Channel (обязательно для Android Oreo+)
        createNotificationChannel(context)

        // Создаем само уведомление
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background) // Иконка уведомления
            .setContentTitle("Напоминание о событии!")
            .setContentText("${eventDate} ${eventTitle}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Отправляем уведомление
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        } catch (se: SecurityException) {
            Log.w("MyLog", "Attempted to send a notification but notifications were disabled.")
        }
    }

    // Функция для проверки разрешения на уведомления
    private fun areNotificationsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.areNotificationsEnabled()
        }
        return true
    }

    // Запрос разрешения на уведомления
    private fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Event Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming events."
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "event_channel_01"
        const val NOTIFICATION_ID = 1
    }
}