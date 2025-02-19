package com.viictrp.quickrates.widget.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.viictrp.quickrates.R
import com.viictrp.quickrates.client.dto.CurrencyDTO
import com.viictrp.quickrates.widget.QuickRatesWidgets
import com.viictrp.quickrates.widget.WidgetEntryPoint
import com.viictrp.quickrates.widget.updateViews
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notification = createNotification()
        setForegroundAsync(ForegroundInfo(1, notification))

        Log.d("WidgetUpdateWorker", "Updating widget...")

        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val client = entryPoint.currencyClient()

        return withContext(Dispatchers.IO) {
            val currency = client.fetchCurrency()

            updateWidget(currency)
//            scheduleNextWorker(context)

            Log.d("WidgetUpdateWorker", "Widget updated with currency: ${currency?.bid}")
            Result.success()
        }
    }

    private fun createNotification(): Notification {
        val channelId = "widget_update_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Widget Updates",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel) // Adiciona null safety
        }

        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Atualizando Widget")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }

    private fun scheduleNextWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS) // Run every 1 second
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun updateWidget(currency: CurrencyDTO?) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, QuickRatesWidgets::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.quick_rates_widgets)
            updateViews(views, currency, context)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}