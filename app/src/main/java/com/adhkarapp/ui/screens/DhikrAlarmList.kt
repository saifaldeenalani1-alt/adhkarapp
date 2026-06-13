package com.adhkarapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adhkarapp.data.DhikrData
import com.adhkarapp.data.PreferencesManager
import com.adhkarapp.model.DhikrAlarm
import com.adhkarapp.util.DhikrScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrAlarmList() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var alarms by remember { mutableStateOf(prefs.getDhikrAlarms()) }
    var showDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<DhikrAlarm?>(null) }

    fun refresh() { alarms = prefs.getDhikrAlarms() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (alarms.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("لا توجد منبهات أذكار", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("اضغط على + لإضافة منبه جديد", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    DhikrAlarmCard(
                        alarm = alarm,
                        onToggle = {
                            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
                            prefs.saveDhikrAlarm(updated)
                            if (updated.isEnabled) DhikrScheduler.schedule(context, updated)
                            else DhikrScheduler.cancel(context, updated)
                            refresh()
                        },
                        onEdit = { editingAlarm = it; showDialog = true },
                        onDelete = {
                            DhikrScheduler.cancel(context, alarm)
                            prefs.deleteDhikrAlarm(alarm.id)
                            refresh()
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { editingAlarm = null; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "إضافة منبه")
        }
    }

    if (showDialog) {
        AddDhikrDialog(
            alarm = editingAlarm,
            onDismiss = { showDialog = false; editingAlarm = null },
            onSave = { alarm ->
                prefs.saveDhikrAlarm(alarm)
                if (alarm.isEnabled) DhikrScheduler.schedule(context, alarm)
                else DhikrScheduler.cancel(context, alarm)
                refresh()
                showDialog = false
                editingAlarm = null
            }
        )
    }
}

@Composable
fun DhikrAlarmCard(
    alarm: DhikrAlarm,
    onToggle: () -> Unit,
    onEdit: (DhikrAlarm) -> Unit,
    onDelete: () -> Unit
) {
    val count = alarm.dhikrIds.size
    val names = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    val dayStr = if (alarm.repeatDays.size == 7) "كل الأيام"
    else alarm.repeatDays.sorted().map { names[it - 1] }.joinToString("، ")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = alarm.label.ifEmpty { "منبه أذكار" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = alarm.isEnabled, onCheckedChange = { onToggle() })
            }
            Spacer(Modifier.height(4.dp))
            Text("${alarm.startHour.toString().padStart(2, '0')}:${alarm.startMinute.toString().padStart(2, '0')} - ${alarm.endHour.toString().padStart(2, '0')}:${alarm.endMinute.toString().padStart(2, '0')}", style = MaterialTheme.typography.bodyMedium)
            Text("كل ${alarm.intervalMinutes} دقيقة", style = MaterialTheme.typography.bodyMedium)
            Text("$count أذكار مختارة", style = MaterialTheme.typography.bodySmall)
            Text(dayStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (alarm.playAudio) {
                Text("🔊 قراءة صوتية", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { onEdit(alarm) }) {
                    Icon(Icons.Default.Edit, "تعديل", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
