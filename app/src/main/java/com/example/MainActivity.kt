package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ScreenTab
import com.example.ui.TongueScanViewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ModelConfigScreen
import com.example.ui.screens.ProjectGuideScreen
import com.example.ui.screens.ScreeningScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: TongueScanViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val historyRecords by viewModel.historyList.collectAsStateWithLifecycle()

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            CenterAlignedTopAppBar(
              title = {
                Text(
                  text = "TongueScan AI",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 19.sp,
                  letterSpacing = 0.5.sp
                )
              },
              colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
              )
            )
          },
          bottomBar = {
            NavigationBar(
              modifier = Modifier.testTag("main_bottom_nav"),
              containerColor = MaterialTheme.colorScheme.surface
            ) {
              NavigationBarItem(
                selected = uiState.selectedTab == ScreenTab.SCREENING,
                onClick = { viewModel.setTab(ScreenTab.SCREENING) },
                icon = {
                  Icon(
                    imageVector = if (uiState.selectedTab == ScreenTab.SCREENING) Icons.Filled.MedicalServices else Icons.Outlined.MedicalServices,
                    contentDescription = "Screening"
                  )
                },
                label = { Text("Screening") },
                modifier = Modifier.testTag("nav_tab_screening")
              )

              NavigationBarItem(
                selected = uiState.selectedTab == ScreenTab.HISTORY,
                onClick = { viewModel.setTab(ScreenTab.HISTORY) },
                icon = {
                  Icon(
                    imageVector = if (uiState.selectedTab == ScreenTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = "Records"
                  )
                },
                label = { Text("Records") },
                modifier = Modifier.testTag("nav_tab_history")
              )

              NavigationBarItem(
                selected = uiState.selectedTab == ScreenTab.MODEL_CONFIG,
                onClick = { viewModel.setTab(ScreenTab.MODEL_CONFIG) },
                icon = {
                  Icon(
                    imageVector = if (uiState.selectedTab == ScreenTab.MODEL_CONFIG) Icons.Filled.Tune else Icons.Outlined.Tune,
                    contentDescription = "Model"
                  )
                },
                label = { Text("Model Setup") },
                modifier = Modifier.testTag("nav_tab_model_config")
              )

              NavigationBarItem(
                selected = uiState.selectedTab == ScreenTab.GUIDE,
                onClick = { viewModel.setTab(ScreenTab.GUIDE) },
                icon = {
                  Icon(
                    imageVector = if (uiState.selectedTab == ScreenTab.GUIDE) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                    contentDescription = "Guide"
                  )
                },
                label = { Text("Thesis Guide") },
                modifier = Modifier.testTag("nav_tab_guide")
              )
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (uiState.selectedTab) {
              ScreenTab.SCREENING -> {
                ScreeningScreen(
                  uiState = uiState,
                  onImageSelected = { uri -> viewModel.onImageSelected(uri) },
                  onSampleSelected = { type -> viewModel.onSampleSelected(type) },
                  onClear = { viewModel.clearSelection() },
                  onAnalyze = { viewModel.analyzeImage() },
                  onNavigateToConfig = { viewModel.setTab(ScreenTab.MODEL_CONFIG) },
                  onNavigateToGuide = { viewModel.setTab(ScreenTab.GUIDE) }
                )
              }

              ScreenTab.HISTORY -> {
                HistoryScreen(
                  records = historyRecords,
                  onDeleteRecord = { id -> viewModel.deleteHistory(id) },
                  onClearAll = { viewModel.clearAllHistory() }
                )
              }

              ScreenTab.MODEL_CONFIG -> {
                ModelConfigScreen(
                  uiState = uiState,
                  onUpdateBackendUrl = { url -> viewModel.updateBackendUrl(url) },
                  onRefreshStatus = { viewModel.checkBackendModelStatus() }
                )
              }

              ScreenTab.GUIDE -> {
                ProjectGuideScreen()
              }
            }
          }
        }
      }
    }
  }
}
