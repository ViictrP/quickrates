package com.viictrp.quickrates.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import com.viictrp.quickrates.R
import com.viictrp.quickrates.client.CurrencyClient
import com.viictrp.quickrates.client.dto.CurrencyDTO
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        super.onEnabled(context)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, QuickRatesWidgets::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
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

    CoroutineScope(Dispatchers.IO).launch {
        val currency = client.fetchCurrency()
        updateViews(views, currency, context)

        Log.d("QuickRatesWidgets", "Currency fetched: ${currency?.bid}")

        CoroutineScope(Dispatchers.Main).launch {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

fun updateViews(views: RemoteViews, currency: CurrencyDTO?, context: Context) {
    val formattedValue = String.format(Locale.US, "%.2f", currency?.bid?.toDoubleOrNull() ?: 0.0)
    views.setTextViewText(R.id.value, formattedValue)
    views.setTextViewText(R.id.var_bid, currency?.varBid ?: "---")

    val formattedPctChange = String.format(Locale.US, "%.2f", currency?.pctChange?.toDoubleOrNull() ?: 0.0) + "%"
    views.setTextViewText(R.id.pct_change, formattedPctChange)

    val color = if (currency?.varBid?.contains("-") == true) context.getColor(R.color.loss) else context.getColor(R.color.gain)
    views.setTextColor(R.id.var_bid, color);
    views.setTextColor(R.id.pct_change, color);
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun currencyClient(): CurrencyClient
}