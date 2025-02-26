package com.viictrp.quickrates.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.viictrp.quickrates.R
import com.viictrp.quickrates.client.CurrencyClient
import com.viictrp.quickrates.client.dto.CurrencyDTO
import com.viictrp.quickrates.widget.worker.scheduleWidgetUpdate
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class QuickRatesWidgets : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("QuickRatesWidgets", "Widget onUpdate triggered")

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, QuickRatesWidgets::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleWidgetUpdate(context)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d("QuickRatesWidgets", "Received intent with action: ${intent.action}")

        if (intent.action == "com.viictrp.quickrates.widget.UPDATE_WIDGET") {
            Log.d("QuickRatesWidgets", "Widget update action received")

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, QuickRatesWidgets::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Log.d("QuickRatesWidgets", "Updating widget with appWidgetId: $appWidgetId")

    val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
    val client = entryPoint.currencyClient()

    val views = RemoteViews(context.packageName, R.layout.quick_rates_widgets)
    views.setOnClickPendingIntent(R.id.main_layout, getPendingIntent(context, appWidgetId))
    views.setViewVisibility(R.id.loading, View.VISIBLE)

    appWidgetManager.updateAppWidget(appWidgetId, views)

    // Use a single CoroutineScope to avoid leaks
    CoroutineScope(Dispatchers.IO).launch {
        val currency = client.fetchCurrency()
        Log.d("QuickRatesWidgets", "Currency fetched: ${currency?.bid}")

        updateViews(views, currency, context)
        views.setViewVisibility(R.id.loading, View.INVISIBLE)

        // Ensure UI updates are on the main thread
        withContext(Dispatchers.Main) {
            Log.d("QuickRatesWidgets", "Persisting the updates")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

fun getPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
    val intent = Intent(context, QuickRatesWidgets::class.java).apply {
        action = "com.viictrp.quickrates.widget.UPDATE_WIDGET" // Ensure it's exactly the same
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    Log.d("QuickRatesWidgets", "Creating PendingIntent for action: ${intent.action}")

    return PendingIntent.getBroadcast(
        context,
        System.currentTimeMillis().toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

fun updateViews(views: RemoteViews, currency: CurrencyDTO?, context: Context) {
    Log.d("QuickRatesWidgets", "Updating widget's views")
    val formattedValue = String.format(Locale.US, "%.2f", currency?.bid?.toDoubleOrNull() ?: "---")
    views.setTextViewText(R.id.value, formattedValue)

    val formattedVarBid =
        String.format(Locale.US, "%.2f", currency?.varBid?.toDoubleOrNull() ?: "---")
    views.setTextViewText(R.id.var_bid, formattedVarBid)

    val formattedPctChange =
        String.format(Locale.US, "%.2f", currency?.pctChange?.toDoubleOrNull() ?: 0.0) + "%"
    views.setTextViewText(R.id.pct_change, formattedPctChange)

    val color =
        if (currency?.varBid?.contains("-") == true) context.getColor(R.color.loss) else context.getColor(
            R.color.gain
        )
    views.setTextColor(R.id.var_bid, color);
    views.setTextColor(R.id.pct_change, color);
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun currencyClient(): CurrencyClient
}