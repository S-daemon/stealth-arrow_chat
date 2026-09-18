package com.example.arrows

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.arrows.chat.SecretChatScreen
import com.example.arrows.game.ArrowsGameScreen
import com.example.arrows.theme.ArrowsTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.initializeApp

sealed interface AppScreen {
    data object Game : AppScreen
    data class SecretChat(val room: String, val pass: String) : AppScreen
}

class MainActivity : ComponentActivity(), SensorEventListener {
    private var currentScreen by mutableStateOf<AppScreen>(AppScreen.Game)
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Accelerometer for Shake-to-Panic feature
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        enableEdgeToEdge()
Firebase.initializeApp(this)
        setContent {
            ArrowsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen) {
                        is AppScreen.Game -> {
                            ArrowsGameScreen(
                                onUnlockSecretChat = { room, pass ->
                                    enableFlagSecure(true)
                                    currentScreen = AppScreen.SecretChat(room, pass)
                                }
                            )
                        }
                        is AppScreen.SecretChat -> {
                            SecretChatScreen(
                                roomName = screen.room,
                                passphrase = screen.pass,
                                onPanicEscape = {
                                    triggerPanicEscape()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        // Automatically drop back to game if the app is paused / backgrounded
        if (currentScreen is AppScreen.SecretChat) {
            triggerPanicEscape()
        }
    }

    /**
     * Controls Android FLAG_SECURE.
     * When enabled, prevents screenshots, screen recording, and blacks out recent apps preview.
     */
    private fun enableFlagSecure(enable: Boolean) {
        runOnUiThread {
            if (enable) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    /**
     * Instantly wipes sensitive chat state and returns to the Arrows Game cover.
     */
    private fun triggerPanicEscape() {
        enableFlagSecure(false)
        currentScreen = AppScreen.Game
    }

    // Accelerometer Shake Detection
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || currentScreen !is AppScreen.SecretChat) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gForce = sqrt((x * x + y * y + z * z).toDouble()) / SensorManager.GRAVITY_EARTH
        if (gForce > 2.7) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 1000) {
                lastShakeTime = now
                triggerPanicEscape()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
