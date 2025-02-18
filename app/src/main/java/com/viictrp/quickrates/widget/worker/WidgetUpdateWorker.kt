package com.viictrp.quickrates.widget.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.viictrp.quickrates.R
import com.viictrp.quickrates.client.dto.CurrencyDTO
import com.viictrp.quickrates.widget.QuickRatesWidgets
import com.viictrp.quickrates.widget.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
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
            views.setTextViewText(R.id.value, currency?.bid ?: "N/A")
            views.setTextViewText(R.id.var_bid, currency?.varBid ?: "N/A")

            val formattedPctChange = String.format(Locale.US, "%.2f", currency?.pctChange?.toDoubleOrNull() ?: 0.0) + "%"
            views.setTextViewText(R.id.pct_change, formattedPctChange)

            val color = if (currency?.varBid?.contains("-") == true) context.getColor(R.color.loss) else context.getColor(R.color.gain)
            views.setTextColor(R.id.value, color)
            views.setTextColor(R.id.var_bid, color)
            views.setTextColor(R.id.pct_change, color)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}