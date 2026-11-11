package com.iturrizaga.fairplay

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.iturrizaga.fairplay.core.app.AppViewModel
import com.iturrizaga.fairplay.ui.AppContent
import com.iturrizaga.fairplay.ui.theme.FairplayTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsState()
            val locale = state.language.locale
            val localizedContext = LocalContext.current.withLocale(locale)

            CompositionLocalProvider(LocalContext provides localizedContext) {
                FairplayTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                        AppContent(viewModel)
                    }
                }
            }
        }
    }
}

private fun Context.withLocale(locale: Locale): Context {
    val config = Configuration(resources.configuration).apply { setLocale(locale) }
    return createConfigurationContext(config)
}
