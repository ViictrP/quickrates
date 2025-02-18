package com.viictrp.quickrates

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.viictrp.quickrates.ui.theme.QuickRatesTheme
import com.viictrp.quickrates.widget.worker.WidgetUpdateWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleWidgetUpdate(applicationContext)
        setContent {
            QuickRatesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QuickRatesTheme {
        Greeting("Android")
    }
}

fun scheduleWidgetUpdate(context: Context) {
    val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
        .setInitialDelay(15, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "widget_update_work",
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
}