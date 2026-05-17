package com.gramasanjeevini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gramasanjeevini.ui.App
import com.gramasanjeevini.ui.theme.GramaSanjeeviniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GramaSanjeeviniTheme {
                App()
            }
        }
    }
}

