package com.example.arrows.game

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArrowsGameScreen(
    onUnlockSecretChat: (room: String, pass: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
    var currentLevelIndex by remember { mutableIntStateOf(0) }
    val currentLevel = Levels.allLevels[currentLevelIndex]

    var gameState by remember(currentLevelIndex) {
        mutableStateOf(Levels.createGameState(currentLevel))
    }

    var showSecretDialog by remember { mutableStateOf(false) }
    var customSeedInput by remember { mutableStateOf("") }
    var secretPassInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    fun vibrateShort(durationMs: Long = 40) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Deep stealth slate background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Discreet long-press trigger on title for secret unlock
                    Text(
                        text = "ARROWS",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    vibrateShort(120)
                                    showSecretDialog = true
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                gameState = Levels.createGameState(currentLevel)
                                vibrateShort(30)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart Level",
                                tint = Color(0xFF94A3B8)
                            )
                        }

                        // Stealth hidden corner button (looks like a tiny decorative dot)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        vibrateShort(120)
                                        showSecretDialog = true
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF334155), CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Level & Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF1E293B),
    shadowElevation = 4.dp,
    modifier = Modifier.combinedClickable(
        onClick = {},
        onLongClick = {
            vibrateShort(100)
            showSecretDialog = true
        }
    )
) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Level ${currentLevel.levelNumber}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentLevel.title,
                            color = Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Moves: ${gameState.movesCount}",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Main Puzzle Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val rows = gameState.rows
                val cols = gameState.cols

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0 until cols) {
                                val arrowId = "arrow_${r}_${c}"
                                val arrow = gameState.arrows[arrowId]

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (arrow != null && !arrow.isEscaped) {
                                        ArrowTileItem(
                                            arrow = arrow,
                                            onTap = {
                                                coroutineScope.launch {
                                                    val canEscape = gameState.canEscape(arrow)
                                                    if (canEscape) {
                                                        vibrateShort(30)
                                                    } else {
                                                        vibrateShort(80)
                                                    }
                                                    gameState = gameState.tapArrow(arrow.id)
                                                }
                                            },
                                            onShakeComplete = {
                                                gameState = gameState.clearShake(arrow.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Win Overlay
                AnimatedVisibility(
                    visible = gameState.isWon,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BOARD CLEARED!",
                                color = Color(0xFF38BDF8),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Completed in ${gameState.movesCount} moves",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if (currentLevelIndex + 1 < Levels.allLevels.size) {
                                        currentLevelIndex++
                                    } else {
                                        currentLevelIndex = 0
                                    }
                                    vibrateShort(40)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                            ) {
                                Text(
                                    text = if (currentLevelIndex + 1 < Levels.allLevels.size) "Next Level" else "Replay All",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Instructions & Discreet Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Tap arrows with clear path to fly away",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Blocked arrows will collide and bounce",
                    color = Color(0xFF475569),
                    fontSize = 11.sp
                )
            }
        }
    }

    // Stealth Dialog (Disguised as Custom Level Code / Room Access)
    if (showSecretDialog) {
        AlertDialog(
            onDismissRequest = { showSecretDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Level Seed / Secret Room",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter room channel and secret passphrase to enter encrypted terminal.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = customSeedInput,
                        onValueChange = { customSeedInput = it },
                        label = { Text("Room Channel / ID") },
                        placeholder = { Text("e.g. room-alpha") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = secretPassInput,
                        onValueChange = { secretPassInput = it },
                        label = { Text("Secret Passphrase") },
                        placeholder = { Text("Passphrase for E2EE") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val room = customSeedInput.trim().ifEmpty { "default-room" }
                        val pass = secretPassInput.trim().ifEmpty { "default-key" }
                        showSecretDialog = false
                        onUnlockSecretChat(room, pass)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("Enter", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecretDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun ArrowTileItem(
    arrow: ArrowItem,
    onTap: () -> Unit,
    onShakeComplete: () -> Unit
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(arrow.isShaking) {
        if (arrow.isShaking) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 250
                    0f at 0
                    -12f at 50
                    12f at 100
                    -8f at 150
                    8f at 200
                    0f at 250
                }
            )
            onShakeComplete()
        }
    }

    val tileBgColor = if (arrow.isShaking) Color(0xFF7F1D1D) else Color(0xFF1E293B)
    val borderColor = if (arrow.isShaking) Color(0xFFEF4444) else Color(0xFF334155)
    val arrowColor = if (arrow.isShaking) Color(0xFFFCA5A5) else Color(0xFF38BDF8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) }
            .clip(RoundedCornerShape(12.dp))
            .background(tileBgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = "Arrow",
            tint = arrowColor,
            modifier = Modifier
                .size(32.dp)
                .rotate(arrow.direction.rotationDegrees)
        )
    }
}

