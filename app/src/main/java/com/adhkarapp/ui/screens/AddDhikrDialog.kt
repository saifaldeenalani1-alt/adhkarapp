package com.adhkarapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adhkarapp.data.DhikrData
import com.adhkarapp.data.PreferencesManager
import com.adhkarapp.model.DhikrAlarm
import com.adhkarapp.model.DhikrItem
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddDhikrDialog(
    alarm: DhikrAlarm?,
    onDismiss: () -> Unit,
    onSave: (DhikrAlarm) -> Unit
) {
    val isEdit = alarm != null
    var label by remember { mutableStateOf(alarm?.label ?: "") }
    var startHour by remember { mutableIntStateOf(alarm?.startHour ?: 6) }
    var startMinute by remember { mutableIntStateOf(alarm?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(alarm?.endHour ?: 22) }
    var endMinute by remember { mutableIntStateOf(alarm?.endMinute ?: 0) }
    var intervalMinutes by remember { mutableIntStateOf(alarm?.intervalMinutes ?: 15) }
    var playAudio by remember { mutableStateOf(alarm?.playAudio ?: false) }
    var audioRepeat by remember { mutableIntStateOf(alarm?.audioRepeatCount ?: 1) }
    var displaySeconds by remember { mutableIntStateOf(alarm?.displayDurationSeconds ?: 10) }
    var selectedIds by remember { mutableStateOf<Set<String>>(alarm?.dhikrIds ?: emptySet()) }

    var showCategoryDialog by remember { mutableStateOf<String?>(null) }

    val categories = DhikrData.getCategories()
    val allItems = DhikrData.getAll()
    val context = LocalContext.current
    val customItems = remember { PreferencesManager(context).getCustomDhikrItems() }
    val allItemsWithCustom = remember { allItems + customItems }
    val categoryItems: (String) -> List<DhikrItem> = { cat ->
        allItemsWithCustom.filter { it.category == cat }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "تعديل المنبه" else "إضافة منبه") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                item {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("اسم المنبه") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startHour.toString(),
                            onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..23) startHour = it } },
                            label = { Text("ساعة البداية") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startMinute.toString(),
                            onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..59) startMinute = it } },
                            label = { Text("دقيقة البداية") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = endHour.toString(),
                            onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..23) endHour = it } },
                            label = { Text("ساعة النهاية") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endMinute.toString(),
                            onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..59) endMinute = it } },
                            label = { Text("دقيقة النهاية") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = intervalMinutes.toString(),
                        onValueChange = { v -> v.toIntOrNull()?.let { if (it in 1..1440) intervalMinutes = it } },
                        label = { Text("كل (دقيقة)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = displaySeconds.toString(),
                        onValueChange = { v -> v.toIntOrNull()?.let { if (it in 3..300) displaySeconds = it } },
                        label = { Text("مدة العرض (ثانية)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("قراءة صوتية", modifier = Modifier.weight(1f))
                        Switch(checked = playAudio, onCheckedChange = { playAudio = it })
                    }
                    if (playAudio) {
                        OutlinedTextField(
                            value = audioRepeat.toString(),
                            onValueChange = { v -> v.toIntOrNull()?.let { if (it in 1..30) audioRepeat = it } },
                            label = { Text("عدد التكرار الصوتي") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("اختيار الأذكار:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }

                categories.forEach { cat ->
                    item {
                        TextButton(
                            onClick = { showCategoryDialog = cat },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${DhikrData.getCategoryLabel(cat)} (${categoryItems(cat).size})")
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = { showCategoryDialog = "custom" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("أذكاري المخصصة (${customItems.size})")
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text("المختارة: ${selectedIds.size} ذكر", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newAlarm = DhikrAlarm(
                    id = alarm?.id ?: UUID.randomUUID().toString(),
                    label = label,
                    isEnabled = alarm?.isEnabled ?: true,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    intervalMinutes = intervalMinutes,
                    dhikrIds = selectedIds,
                    repeatDays = alarm?.repeatDays ?: (1..7).toSet(),
                    displayDurationSeconds = displaySeconds,
                    playAudio = playAudio,
                    audioRepeatCount = audioRepeat
                )
                onSave(newAlarm)
            }) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )

    if (showCategoryDialog != null) {
        val items = if (showCategoryDialog == "custom") customItems
        else categoryItems(showCategoryDialog!!)

        AlertDialog(
            onDismissRequest = { showCategoryDialog = null },
            title = { Text(
                if (showCategoryDialog == "custom") "أذكاري المخصصة"
                else DhikrData.getCategoryLabel(showCategoryDialog!!)
            ) },
            text = {
                LazyColumn {
                    items(items) { item ->
                        val isSelected = item.id in selectedIds
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) selectedIds + item.id
                                    else selectedIds - item.id
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.text, style = MaterialTheme.typography.bodyMedium)
                                Text(item.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = null }) { Text("تم") }
            }
        )
    }
}
