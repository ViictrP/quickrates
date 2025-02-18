package com.viictrp.quickrates.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.viictrp.quickrates.MainActivity
import com.viictrp.quickrates.R
import com.viictrp.quickrates.client.CurrencyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickRatesWidgets : AppWidgetProvider() {
    private lateinit var client: CurrencyClient

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, client)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    client: CurrencyClient
) {
    val views = RemoteViews(context.packageName, R.layout.quick_rates_widgets)
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    views.setOnClickPendingIntent(R.id.main_layout, pendingIntent)

    val widgetText = context.getString(R.string.appwidget_text)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    CoroutineScope(Dispatchers.IO).launch {
        val currency = client.getCurrency()

        CoroutineScope(Dispatchers.Main).launch {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}