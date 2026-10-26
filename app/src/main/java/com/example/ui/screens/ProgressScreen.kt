package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MoodLevel
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SafetyNoticeBanner
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.DaySummaryPoint
import com.example.ui.viewmodel.WellnessUiState
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    uiState: WellnessUiState,
    onSelectRangeDays: (Int) -> Unit,
    onDeleteMood: (Long) -> Unit,
    onDeleteActivity: (Long) -> Unit,
    onDeleteMindfulness: (Long) -> Unit,
    onDeleteSleep: (Long) -> Unit,
    onDeleteJournal: (Long) -> Unit,
    onDeleteStudySession: (Long) -> Unit
) {
    var historyCategory by rememberSaveable { mutableStateOf("Moods") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("progress_screen_list"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Progress & Reflection",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Celebrate showing up consistently — without rankings or streak pressure.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Date Range Picker
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(7, 14, 30).forEach { days ->
                        FilterChip(
                            selected = uiState.selectedRangeDays == days,
                            onClick = { onSelectRangeDays(days) },
                            label = { Text("Last $days Days") },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("range_chip_$days")
                        )
                    }
                }
            }

            // Summary Cards Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryStatCard(
                            title = "Habits Practiced",
                            value = "${uiState.rangeHabitCompletions}",
                            subtitle = "completions in ${uiState.selectedRangeDays}d",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            title = "Movement",
                            value = "${uiState.rangeMovementMinutes}m",
                            subtitle = "gentle minutes",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryStatCard(
                            title = "Mindful Pauses",
                            value = "${uiState.rangeMindfulnessCount}",
                            subtitle = "breathing & grounding",
                            modifier = Modifier.weight(1f)
                        )
                        val sleepVal = uiState.rangeAvgSleepHours?.let {
                            String.format(Locale.US, "%.1fh", it)
                        } ?: "—"
                        SummaryStatCard(
                            title = "Avg Rest Log",
                            value = sleepVal,
                            subtitle = "${uiState.rangeMoodCheckIns} mood check-ins",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryStatCard(
                            title = "Study Focus",
                            value = "${uiState.rangeStudyMinutes}m",
                            subtitle = "${uiState.rangeLockedStudySessions} phone-locked blocks",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            title = "Daily Study Goal",
                            value = "${uiState.preferences.dailyStudyGoalMinutes}m",
                            subtitle = "${uiState.todayStudyMinutes}m focused today",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Accessible Movement Bar Chart (Last 7 Days)
            item {
                MovementBarChartCard(points = uiState.chartPoints)
            }

            // Accessible Mood Reflection Trend Card (Last 7 Days)
            item {
                MoodTrendCard(points = uiState.chartPoints)
            }

            item {
                SafetyNoticeBanner(text = stringResource(R.string.mind_reflection_disclaimer))
            }

            // Inspect & Delete Stored Entries
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Inspect & Manage Entries",
                        subtitle = "Review or delete individual records stored locally on your device."
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Moods", "Study", "Movement", "Mindfulness", "Sleep", "Journals").forEach { cat ->
                            FilterChip(
                                selected = historyCategory == cat,
                                onClick = { historyCategory = cat },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("history_filter_${cat.lowercase()}")
                            )
                        }
                    }
                }
            }

            item {
                HistoryEntriesList(
                    category = historyCategory,
                    uiState = uiState,
                    onDeleteMood = onDeleteMood,
                    onDeleteActivity = onDeleteActivity,
                    onDeleteMindfulness = onDeleteMindfulness,
                    onDeleteSleep = onDeleteSleep,
                    onDeleteJournal = onDeleteJournal,
                    onDeleteStudySession = onDeleteStudySession
                )
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MovementBarChartCard(points: List<DaySummaryPoint>) {
    val barColor = MaterialTheme.colorScheme.primary
    val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant
    val maxMins = (points.maxOfOrNull { it.movementMinutes } ?: 0).coerceAtLeast(15)
    val accessibleSummary = points.joinToString(", ") { "${it.label}: ${it.movementMinutes} minutes" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("7-Day Movement Rhythm", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Minutes of walking, stretching, or gentle strength",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .semantics {
                        contentDescription = "7 day movement bar chart. $accessibleSummary"
                    }
            ) {
                if (points.isEmpty()) return@Canvas
                val slotWidth = size.width / points.size
                val barWidth = (slotWidth * 0.48f).coerceAtMost(36.dp.toPx())
                points.forEachIndexed { index, pt ->
                    val ratio = (pt.movementMinutes.toFloat() / maxMins.toFloat()).coerceIn(0f, 1f)
                    val barHeight = if (pt.movementMinutes > 0) {
                        (size.height * ratio).coerceAtLeast(10.dp.toPx())
                    } else {
                        6.dp.toPx()
                    }
                    val left = index * slotWidth + (slotWidth - barWidth) / 2f
                    val top = size.height - barHeight
                    drawRoundRect(
                        color = if (pt.movementMinutes > 0) barColor else emptyBarColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                points.forEach { pt ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${pt.movementMinutes}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(pt.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodTrendCard(points: List<DaySummaryPoint>) {
    val lineColor = MaterialTheme.colorScheme.secondary
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    val hasAnyMood = points.any { it.avgMoodScore != null }
    val accessibleMoodDesc = points.joinToString(", ") {
        "${it.label}: ${it.avgMoodScore?.let { s -> String.format(Locale.US, "%.1f", s) } ?: "no entry"}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("7-Day Mood Reflection", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Scale: 1 (Heavy) to 5 (Bright)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (!hasAnyMood) {
                Text(
                    text = "Check in with your mood on the Mind tab to see gentle trends over time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .semantics {
                            contentDescription = "7 day mood reflection chart. $accessibleMoodDesc"
                        }
                ) {
                    val slotWidth = size.width / points.size.coerceAtLeast(1)
                    // Draw horizontal guide lines
                    listOf(0.15f, 0.5f, 0.85f).forEach { yFrac ->
                        drawLine(
                            color = guideColor.copy(alpha = 0.5f),
                            start = Offset(0f, size.height * yFrac),
                            end = Offset(size.width, size.height * yFrac),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val plotted = points.mapIndexedNotNull { idx, pt ->
                        val score = pt.avgMoodScore ?: return@mapIndexedNotNull null
                        val x = idx * slotWidth + slotWidth / 2f
                        val normalized = ((score - 1f) / 4f).coerceIn(0f, 1f)
                        val y = size.height * (0.88f - normalized * 0.76f)
                        Offset(x, y)
                    }

                    if (plotted.size >= 2) {
                        val path = Path().apply {
                            moveTo(plotted.first().x, plotted.first().y)
                            plotted.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    plotted.forEach { pt ->
                        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = pt)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    points.forEach { pt ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = pt.avgMoodScore?.let { String.format(Locale.US, "%.0f", it) } ?: "·",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(pt.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntriesList(
    category: String,
    uiState: WellnessUiState,
    onDeleteMood: (Long) -> Unit,
    onDeleteActivity: (Long) -> Unit,
    onDeleteMindfulness: (Long) -> Unit,
    onDeleteSleep: (Long) -> Unit,
    onDeleteJournal: (Long) -> Unit,
    onDeleteStudySession: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (category) {
            "Moods" -> {
                if (uiState.moodEntries.isEmpty()) {
                    EmptyStateCard(Icons.Outlined.Insights, "No mood logs", "Mood check-ins will appear here.")
                } else {
                    uiState.moodEntries.take(20).forEach { m ->
                        InspectEntryCard(
                            title = "${MoodLevel.fromScore(m.moodScore).label} (${m.moodScore}/5)",
                            subtitle = "${m.dateKey} • ${m.note.ifBlank { "No note" }}",
                            onDelete = { onDeleteMood(m.id) }
                        )
                    }
                }
            }

            "Study" -> {
                if (uiState.studySessions.isEmpty()) {
                    EmptyStateCard(Icons.Outlined.Insights, "No study sessions", "Focused study blocks will appear here.")
                } else {
                    uiState.studySessions.take(20).forEach { st ->
                        val lockTag = if (st.phoneLocked) "Phone Locked" else "Timer"
                        InspectEntryCard(
                            title = "${st.taskTitle} • ${st.durationMinutes} min",
                            subtitle = "${st.dateKey} • ${st.subject} ($lockTag)",
                            onDelete = { onDeleteStudySession(st.id) }
                        )
                    }
                }
            }

            "Movement" -> {
                if (uiState.activityLogs.isEmpty()) {
                    EmptyStateCard(Icons.Outlined.Insights, "No movement logs", "Movement sessions will appear here.")
                } else {
                    uiState.activityLogs.take(20).forEach { a ->
                        InspectEntryCard(
                            title = "${a.title} • ${a.durationMinutes} min",
                            subtitle = "${a.dateKey} • ${a.activityType} (${a.intensity})",
                            onDelete = { onDeleteActivity(a.id) }
                        )
                    }
                }
            }

            "Mindfulness" -> {
                if (uiState.mindfulnessSessions.isEmpty()) {
                    EmptyStateCard(Icons.Outlined.Insights, "No mindfulness logs", "Breathing and grounding sessions appear here.")
                } else {
                    uiState.mindfulnessSessions.take(20).forEach { s ->
                        InspectEntryCard(
                            title = "${s.title} (${s.durationSeconds / 60}m ${s.durationSeconds % 60}s)",
                            subtitle = "${s.dateKey} • ${s.sessionType}",
                            onDelete = { onDeleteMindfulness(s.id) }
                        )
                    }
                }
            }

            "Sleep" -> {
                if (uiState.sleepLogs.isEmpty()) {
                    EmptyStateCard(Icons.Outlined.Insights, "No sleep logs", "Sleep routine records appear here.")
                } else {
                    uiState.sleepLogs.take(20).forEach { sl ->
                        InspectEntryCard(
                            title = "~${sl.approxHours}h (${sl.bedTimeText} → ${sl.wakeTimeText})",
                            subtitle = "${sl.dateKey} • Self-rating ${sl.qualityRating}/5",
                            onDelete = { onDeleteSleep(sl.id) }
                        )
                    }
                }
            }

            else -> {
                if (uiState.journalEntries.isEmpty()) {
                    EmptyStateCard(Icons.Outlined.Insights, "No journal reflections", "Private reflections appear here.")
                } else {
                    uiState.journalEntries.take(20).forEach { j ->
                        InspectEntryCard(
                            title = j.prompt.ifBlank { "Private Reflection" },
                            subtitle = "${j.dateKey} • ${j.content.take(90)}",
                            onDelete = { onDeleteJournal(j.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectEntryCard(
    title: String,
    subtitle: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete $title")
            }
        }
    }
}
