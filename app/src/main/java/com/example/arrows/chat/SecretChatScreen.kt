package com.example.arrows.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.QuerySnapshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arrows.crypto.CryptoUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val isMe: Boolean,
    val plaintext: String,
    val encryptedPayload: String,
    val timestamp: Long = System.currentTimeMillis(),
    val burnDurationSeconds: Int = 30, // 0 = no burn
    val initialTimeRemaining: Int = 30
)

@Composable
fun SecretChatScreen(
    roomName: String,
    passphrase: String,
    onPanicEscape: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Android Back Button triggers Panic escape immediately
    BackHandler {
        onPanicEscape()
    }

    var antiPeepEnabled by remember { mutableStateOf(false) }
    var selectedBurnTimerSeconds by remember { mutableIntStateOf(30) }
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Firestore setup
    val db = Firebase.firestore
    val messagesRef = db.collection("rooms").document(roomName).collection("messages")

    // Listen for real‑time updates
    LaunchedEffect(roomName) {
        messagesRef.addSnapshotListener { snapshot: QuerySnapshot?, _ ->
            snapshot?.documents?.let { docs ->
                val newMessages = docs.mapNotNull { doc ->
                    val encrypted = doc.getString("encryptedPayload") ?: return@mapNotNull null
                    val sender = doc.getString("sender") ?: "Unknown"
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val burn = (doc.getLong("burnDuration") ?: 0L).toInt()
                    val plaintext = CryptoUtils.decrypt(encrypted, passphrase) ?: "[decryption error]"
                    ChatMessage(
                        sender = sender,
                        isMe = sender == "Me",
                        plaintext = plaintext,
                        encryptedPayload = encrypted,
                        timestamp = timestamp,
                        burnDurationSeconds = burn,
                        initialTimeRemaining = burn
                    )
                }
                messages.clear()
                messages.addAll(newMessages)
            }
        }
    }

    // Add initial system message explaining E2EE
    LaunchedEffect(Unit) {
        val welcome = "Room channel '$roomName' established. E2EE active (AES-256-GCM). Messages will self-destruct."
        val enc = CryptoUtils.encrypt(welcome, passphrase)
        messages.add(
            ChatMessage(
                sender = "System",
                isMe = false,
                plaintext = welcome,
                encryptedPayload = enc,
                burnDurationSeconds = 0,
                initialTimeRemaining = 0
            )
        )
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty()) return

        val encrypted = CryptoUtils.encrypt(text, passphrase)
        // Write to Firestore
        messagesRef.add(
            mapOf(
                "sender" to "Me",
                "encryptedPayload" to encrypted,
                "timestamp" to System.currentTimeMillis(),
                "burnDuration" to selectedBurnTimerSeconds
            )
        )
        inputText = ""
        // Scroll to the latest message after a short delay (listener will update UI)
        scope.launch {
            delay(200)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF090D16) // Stealth midnight black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Stealth Top Header
            Surface(
                color = Color(0xFF131B2E),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "#$roomName",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "E2EE AES-256-GCM",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Anti-peep toggle
                            IconButton(onClick = { antiPeepEnabled = !antiPeepEnabled }) {
                                Icon(
                                    imageVector = if (antiPeepEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Anti-Peep Mode",
                                    tint = if (antiPeepEnabled) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                                )
                            }

                            // Burn timer selector button
                            IconButton(
                                onClick = {
                                    selectedBurnTimerSeconds = when (selectedBurnTimerSeconds) {
                                        10 -> 30
                                        30 -> 60
                                        60 -> 0
                                        else -> 10
                                    }
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Burn Timer",
                                        tint = if (selectedBurnTimerSeconds > 0) Color(0xFFEF4444) else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    if (selectedBurnTimerSeconds > 0) {
                                        Text(
                                            text = "${selectedBurnTimerSeconds}s",
                                            color = Color(0xFFEF4444),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // PANIC ESCAPE BUTTON (Instant Return to Game)
                            Button(
                                onClick = onPanicEscape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "PANIC",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        antiPeepEnabled = antiPeepEnabled,
                        onBurnExpired = {
                            messages.remove(message)
                        }
                    )
                }
            }

            // Bottom Input Bar
            Surface(
                color = Color(0xFF131B2E),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Encrypted message...", color = Color(0xFF64748B)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { sendMessage() },
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFF38BDF8), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color(0xFF090D16),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    antiPeepEnabled: Boolean,
    onBurnExpired: () -> Unit
) {
    var isRevealed by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(message.burnDurationSeconds) }

    // Ephemeral Countdown Timer
    LaunchedEffect(message.burnDurationSeconds) {
        if (message.burnDurationSeconds > 0) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            onBurnExpired()
        }
    }

    val isMe = message.isMe
    val isSystem = message.sender == "System"

    val alignment = if (isSystem) Alignment.CenterHorizontally else if (isMe) Alignment.End else Alignment.Start
    val bgColor = if (isSystem) Color(0xFF1E293B) else if (isMe) Color(0xFF0369A1) else Color(0xFF1E293B)
    val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = message.sender,
                color = if (isMe) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeFormatted,
                color = Color(0xFF475569),
                fontSize = 10.sp
            )
            if (message.burnDurationSeconds > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🔥 ${secondsLeft}s",
                    color = Color(0xFFEF4444),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bgColor,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    if (antiPeepEnabled) {
                        isRevealed = !isRevealed
                    }
                }
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                val shouldBlur = antiPeepEnabled && !isRevealed && !isSystem

                Text(
                    text = if (shouldBlur) "•••••••••••••••• (Tap to reveal)" else message.plaintext,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = if (shouldBlur) Modifier.blur(1.dp) else Modifier
                )

                if (message.burnDurationSeconds > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = secondsLeft.toFloat() / message.burnDurationSeconds.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(2.dp)
                            .clip(CircleShape),
                        color = Color(0xFFEF4444),
                        trackColor = Color(0xFF334155),
                    )
                }
            }
        }
    }
}

