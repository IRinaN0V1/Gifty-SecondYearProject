package com.example.gifty.Adapters

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gifty.R

class EventNotificationReceiver : BroadcastReceiver() {

    // Метод вызывается системой при получении сигнала (Broadcast)
    override fun onReceive(context: Context, intent: Intent) {
        // Получаем название и дату события из полученного интента
        val eventTitle = intent.getStringExtra("event_title")
        val eventDate = intent.getStringExtra("event_date")

        // Проверяем разрешена ли отправка уведомлений устройством
        if (!areNotificationsEnabled(context)) {
            // Если уведомления запрещены, запрашиваем разрешение у пользователя
            requestNotificationPermission(context)
            return
        }

        // Создаем канал уведомлений
        createNotificationChannel(context)

        // Строим объект уведомления
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Напоминание о событии!")
            .setContentText("$eventDate $eventTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Отправляем уведомление
        try {
            // Получаем менеджер уведомлений и отправляем уведомление
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        } catch (se: SecurityException) {
            Log.w("MyLog", "Попытка отправить уведомление, но уведомления были отключены.")
        }
    }

    // Проверяет, разрешены ли уведомления приложением
    private fun areNotificationsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.areNotificationsEnabled()
        }
        return true
    }

    // Запрашивает у пользователя разрешение на получение уведомлений
    private fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Формируем интент для перехода в настройки уведомлений нашего приложения
            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent) // Запускаем окно настроек уведомлений
        }
    }

    // Создает канал уведомлений для Android Oreo+
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Создаем канал уведомлений
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Event Reminders",
                NotificationManager.IMPORTANCE_DEFAULT // Важность уведомлений
            )
            channel.description = "Notifications for upcoming events." // Описание канала
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // Константы для каналов и идентификаторов уведомлений
    companion object {
        const val CHANNEL_ID = "event_channel_01" // Уникальный идентификатор канала
        const val NOTIFICATION_ID = 1 // Идентификатор уведомления
    }
}