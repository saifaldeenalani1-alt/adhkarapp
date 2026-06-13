package com.adhkarapp.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adhkarapp.data.DhikrData
import com.adhkarapp.data.PreferencesManager
import com.adhkarapp.model.DhikrItem
import com.adhkarapp.util.TtsHelper

@Composable
fun TasbihScreen() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var state by remember { mutableStateOf(prefs.getTasbihState()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDhikrDialog by remember { mutableStateOf(false) }

    val target = 100
    val remaining = (target - state.count).coerceAtLeast(0)
    val progress = (state.count.toFloat() / target).coerceIn(0f, 1f)

    LaunchedEffect(state.count) {
        if (state.count >= target && state.count > 0) {
            prefs.saveTasbihState(state)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
            if (state.ttsEnabled) {
                TtsHelper.init(context) {
                    val text = state.dhikrItem?.text ?: "سبحان الله وبحمده"
                    TtsHelper.speak(text, 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = state.dhikrItem?.text ?: "سبحان الله وبحمده",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        state.dhikrItem?.source?.let { src ->
            Text(
                text = src,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = remaining.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = state.count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                state = state.copy(count = state.count + 1)
                prefs.saveTasbihState(state)
            },
            modifier = Modifier.size(200.dp, 64.dp),
            shape = CircleShape
        ) {
            Text("تسبيح", fontSize = 20.sp)
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = {
                state = state.copy(count = 0)
                prefs.saveTasbihState(state)
            }) {
                Icon(Icons.Default.Refresh, "إعادة تعيين")
                Spacer(Modifier.width(4.dp))
                Text("إعادة")
            }
            OutlinedButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Settings, "تغيير الذكر")
                Spacer(Modifier.width(4.dp))
                Text("تغيير")
            }
        }
    }

    if (showEditDialog) {
        TasbihSettingsDialog(
            currentDhikr = state.dhikrItem,
            ttsEnabled = state.ttsEnabled,
            onDismiss = { showEditDialog = false },
            onSave = { item, tts ->
                state = state.copy(dhikrItem = item, ttsEnabled = tts)
                prefs.saveTasbihState(state)
                showEditDialog = false
            },
            onAddCustomClick = {
                showEditDialog = false
                showAddDhikrDialog = true
            }
        )
    }

    if (showAddDhikrDialog) {
        AddCustomDhikrDialog(
            onDismiss = { showAddDhikrDialog = false },
            onSave = { item ->
                prefs.saveCustomDhikrItem(item)
                state = state.copy(dhikrItem = item)
                prefs.saveTasbihState(state)
                showAddDhikrDialog = false
            }
        )
    }
}

@Composable
fun TasbihSettingsDialog(
    currentDhikr: DhikrItem?,
    ttsEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (DhikrItem?, Boolean) -> Unit,
    onAddCustomClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    val allItems = remember { DhikrData.all }
    val customItems = remember { prefs.getCustomDhikrItems() }
    val allWithCustom = remember { allItems + customItems }
    var selected by remember { mutableStateOf(currentDhikr) }
    var tts by remember { mutableStateOf(ttsEnabled) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    if (showCategoryPicker) {
        val categories = DhikrData.getCategories()
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("اختر ذكراً") },
            text = {
                LazyColumn {
                    items(categories) { cat ->
                        TextButton(
                            onClick = {
                                selected = DhikrData.getByCategory(cat).firstOrNull()
                                showCategoryPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(DhikrData.getCategoryLabel(cat))
                        }
                    }
                    if (customItems.isNotEmpty()) {
                        item {
                            Divider()
                            Text("أذكاري المخصصة", style = MaterialTheme.typography.labelLarge)
                        }
                        items(customItems) { item ->
                            TextButton(
                                onClick = { selected = item; showCategoryPicker = false },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(item.text) }
                        }
                    }
                    item {
                        TextButton(
                            onClick = { showCategoryPicker = false; onAddCustomClick() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("+ إضافة ذكر جديد") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCategoryPicker = false }) { Text("إلغاء") } }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعدادات التسبيح") },
        text = {
            Column {
                Text(selected?.text ?: "لم يتم اختيار ذكر", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { showCategoryPicker = true }) {
                    Text("تغيير الذكر")
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("قراءة صوتية عند الوصول للهدف", modifier = Modifier.weight(1f))
                    Switch(checked = tts, onCheckedChange = { tts = it })
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(selected, tts) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomDhikrDialog(
    onDismiss: () -> Unit,
    onSave: (DhikrItem) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة ذكر مخصص") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("نص الذكر") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("المصدر (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) {
                    val item = DhikrItem(
                        id = "custom_${System.currentTimeMillis()}",
                        text = text.trim(),
                        source = source.trim(),
                        category = "custom",
                        count = 1
                    )
                    onSave(item)
                }
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
