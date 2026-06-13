package com.adhkarapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.adhkarapp.ui.theme.AdhkarTheme

data class TabItem(val title: String, val icon: ImageVector, val screen: @Composable () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            TabItem("منبه", Icons.Default.Alarm) { DhikrAlarmList() },
            TabItem("مسبحة", Icons.Default.FilterCenterFocus) { TasbihScreen() }
        )
    }

    AdhkarTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(tabs[selectedTab].title) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                tabs[selectedTab].screen()
            }
        }
    }
}
