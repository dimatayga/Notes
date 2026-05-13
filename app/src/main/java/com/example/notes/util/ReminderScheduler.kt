package com.example.notes.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.notes.domain.model.Note

import android.os.Build

object ReminderScheduler {
    fun schedule(context: Context, note: Note) {
        val reminderTime = note.reminderTime ?: return
        if (reminderTime < System.currentTimeMillis()) return

        val intent = Intent(context, NoteReminderReceiver::class.java).apply {
            putExtra("title", note.title)
            putExtra("content", note.content)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, note: Note) {
        val intent = Intent(context, NoteReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id ?: 0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}
