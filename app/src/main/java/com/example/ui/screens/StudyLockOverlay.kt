package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.util.PhoneLockController
import com.example.ui.viewmodel.ActiveStudySessionState
import kotlinx.coroutines.delay

@Composable
fun StudyLockOverlay(
    session: ActiveStudySessionState,
    onTogglePause: () -> Unit,
    onUnlockAndFinish: () -> Unit,
    onDistractionDetected: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { PhoneLockController.findActivity(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var showUnlockConfirm by rememberSaveable { mutableStateOf(false) }
    var isOsPinned by remember { mutableStateOf(PhoneLockController.isOsScreenPinningActive(context)) }

    // Poll OS screen pinning state lightly so UI badge updates immediately after user accepts OS pin prompt
    LaunchedEffect(session.isActive) {
        while (session.isActive) {
            isOsPinned = PhoneLockController.isOsScreenPinningActive(context)
            delay(1500L)
        }
    }

    // Engage native Android Screen Pinning + immersive system bars on enter, release cleanly on exit
    DisposableEffect(session.requestOsPinning, session.keepScreenAwake) {
        PhoneLockController.engageStudyLock(
            activity = activity,
            requestOsPinning = session.requestOsPinning,
            keepScreenOn = session.keepScreenAwake
        )
        onDispose {
            PhoneLockController.releaseStudyLock(activity)
        }
    }

    // Detect if the user leaves the app while unpinned and gently call them back to focus
    DisposableEffect(lifecycleOwner, session.isActive, session.isBreakPhase) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP &&
                session.isActive &&
                !session.isBreakPhase &&
                activity?.isChangingConfigurations != true
            ) {
                onDistractionDetected()
                PhoneLockController.sendReturnToFocusNotification(context, session.taskTitle)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Intercept hardware/gesture Back presses during locked study time
    BackHandler(enabled = true) {
        showUnlockConfirm = true
    }

    val deepShieldGradient = Brush.verticalGradient(
        colors = if (session.isBreakPhase) {
            listOf(Color(0xFF1A3A2D), Color(0xFF10241C))
        } else {
            listOf(Color(0xFF101915), Color(0xFF182921), Color(0xFF0E1612))
        }
    )

    val softCream = Color(0xFFFAF8F3)
    val sageAccent = Color(0xFF93CAAC)
    val warmTerracotta = Color(0xFFEBA588)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(deepShieldGradient)
            .testTag("study_lock_overlay")
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Lock Status Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = sageAccent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, sageAccent.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (session.isBreakPhase) Icons.Outlined.Spa else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = sageAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                session.isBreakPhase -> "Mindful Study Break"
                                isOsPinned -> "Phone Pinned & Locked for Study"
                                else -> "Study Lock Shield Active"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = softCream
                        )
                    }
                }

                if (!isOsPinned && !session.isBreakPhase) {
                    TextButton(
                        onClick = {
                            PhoneLockController.engageStudyLock(
                                activity = activity,
                                requestOsPinning = true,
                                keepScreenOn = session.keepScreenAwake
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("study_repin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = null,
                            tint = warmTerracotta,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pin Screen (Block Home & Other Apps)",
                            style = MaterialTheme.typography.labelMedium,
                            color = warmTerracotta
                        )
                    }
                }
            }

            // Center Task + Countdown Ring
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = session.subject.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = sageAccent
                )
                Text(
                    text = if (session.isBreakPhase) {
                        "Rest your eyes, sip water, or roll your shoulders"
                    } else {
                        session.taskTitle
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = softCream,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.size(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = if (session.totalPhaseSeconds > 0) {
                        1f - (session.remainingSeconds.toFloat() / session.totalPhaseSeconds.toFloat())
                    } else 0f

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 12.dp.toPx()
                        val radius = (size.minDimension / 2f) - stroke
                        drawCircle(
                            color = softCream.copy(alpha = 0.12f),
                            radius = radius,
                            style = Stroke(width = stroke)
                        )
                        drawArc(
                            color = if (session.isBreakPhase) warmTerracotta else sageAccent,
                            startAngle = -90f,
                            sweepAngle = 360f * progress.coerceIn(0f, 1f),
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val mins = session.remainingSeconds / 60
                        val secs = session.remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = softCream,
                            modifier = Modifier.testTag("study_lock_countdown")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                session.isPaused -> "Paused"
                                session.isBreakPhase -> "Break time remaining"
                                else -> "Stay gently with this one task"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = softCream.copy(alpha = 0.78f)
                        )
                    }
                }

                if (session.distractionCount > 0) {
                    Text(
                        text = "Returned to focus ${session.distractionCount} time(s) — glad you're back.",
                        style = MaterialTheme.typography.labelMedium,
                        color = warmTerracotta
                    )
                }
            }

            // Bottom Lock Controls + Emergency Dial
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    OutlinedButton(
                        onClick = onTogglePause,
                        border = BorderStroke(1.dp, softCream.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = softCream),
                        modifier = Modifier
                            .height(52.dp)
                            .weight(1f)
                            .testTag("study_lock_pause_button")
                    ) {
                        Icon(
                            imageVector = if (session.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (session.isPaused) "Resume" else "Pause")
                    }

                    Button(
                        onClick = { showUnlockConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = softCream.copy(alpha = 0.16f),
                            contentColor = softCream
                        ),
                        modifier = Modifier
                            .height(52.dp)
                            .weight(1f)
                            .testTag("study_lock_unlock_button")
                    ) {
                        Icon(Icons.Filled.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (session.isBreakPhase) "End Break" else "Unlock Phone")
                    }
                }

                TextButton(
                    onClick = {
                        PhoneLockController.releaseStudyLock(activity)
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
                        runCatching { context.startActivity(dialIntent) }
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = softCream.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Emergency Phone Dialer",
                        style = MaterialTheme.typography.labelSmall,
                        color = softCream.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showUnlockConfirm) {
        val elapsedMinutes = (session.elapsedFocusSeconds + 30) / 60
        AlertDialog(
            onDismissRequest = { showUnlockConfirm = false },
            title = { Text("Unlock phone early?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = if (session.isBreakPhase) {
                        "Your study block is already logged! Ready to unlock your phone and finish the break?"
                    } else if (elapsedMinutes >= 1) {
                        "Take a slow breath before unlocking. If you stop now, we'll unpin your screen and still log the $elapsedMinutes min you focused so far."
                    } else {
                        "Take a slow breath before unlocking. Staying just 2 more minutes often gets past initial resistance — or tap Unlock to exit now."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlockConfirm = false
                        PhoneLockController.releaseStudyLock(activity)
                        onUnlockAndFinish()
                    },
                    modifier = Modifier.testTag("study_lock_confirm_unlock_button")
                ) {
                    Text("Unlock & Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockConfirm = false }) {
                    Text("Stay Focused")
                }
            }
        )
    }
}
