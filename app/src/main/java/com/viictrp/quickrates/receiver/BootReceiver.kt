package com.viictrp.quickrates.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.viictrp.quickrates.widget.worker.scheduleWidgetUpdate

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Re-scheduling widget update worker after reboot")
            scheduleWidgetUpdate(context)
        }
    }
}