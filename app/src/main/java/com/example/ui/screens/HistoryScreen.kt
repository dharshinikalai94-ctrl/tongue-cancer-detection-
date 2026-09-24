package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScreeningRecord
import com.example.ui.theme.DiagnosticBenignBg
import com.example.ui.theme.DiagnosticBenignGreen
import com.example.ui.theme.DiagnosticMalignantBg
import com.example.ui.theme.DiagnosticMalignantRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  records: List<ScreeningRecord>,
  onDeleteRecord: (Long) -> Unit,
  onClearAll: () -> Unit
) {
  var showClearConfirm by remember { mutableStateOf(false) }
  var selectedRecordForDetail by remember { mutableStateOf<ScreeningRecord?>(null) }

  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Screening History",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${records.size} local examination records saved (Room DB)",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (records.isNotEmpty()) {
        OutlinedButton(
          onClick = { showClearConfirm = true },
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("clear_all_history_button")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.size(4.dp))
          Text("Clear All", fontSize = 12.sp)
        }
      }
    }

    if (records.isEmpty()) {
      // Empty state
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(48.dp)
          )
          Text(
            text = "No Screening Records Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Completed tongue analyses will be stored here locally.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(records, key = { it.id }) { record ->
          ScreeningRecordItem(
            record = record,
            formattedDate = dateFormat.format(Date(record.timestamp)),
            onItemClick = { selectedRecordForDetail = record },
            onDelete = { onDeleteRecord(record.id) }
          )
        }
      }
    }
  }

  // Clear All Dialog
  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      title = { Text("Clear All History?") },
      text = { Text("This will permanently remove all locally cached screening analyses.") },
      confirmButton = {
        TextButton(
          onClick = {
            onClearAll()
            showClearConfirm = false
          }
        ) {
          Text("Clear All", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Record Detail Modal
  if (selectedRecordForDetail != null) {
    val record = selectedRecordForDetail!!
    AlertDialog(
      onDismissRequest = { selectedRecordForDetail = null },
      title = {
        Text(
          text = record.predictedClass,
          fontWeight = FontWeight.Bold,
          color = if (record.isMalignantRisk) DiagnosticMalignantRed else DiagnosticBenignGreen
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Model: ${record.modelName}", fontWeight = FontWeight.SemiBold)
          Text("Confidence: ${record.confidencePercentage}")
          Text("Screened on: ${dateFormat.format(Date(record.timestamp))}")
          Text("Source: ${record.backendSource}")
          Spacer(modifier = Modifier.height(4.dp))
          Text("Clinical Explanation:", fontWeight = FontWeight.SemiBold)
          Text(record.explanation, style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Disclaimer: This result is intended only for preliminary screening support. It does not confirm or rule out tongue cancer. Please consult a qualified healthcare professional.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { selectedRecordForDetail = null }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
fun ScreeningRecordItem(
  record: ScreeningRecord,
  formattedDate: String,
  onItemClick: () -> Unit,
  onDelete: () -> Unit
) {
  val isMalignant = record.isMalignantRisk
  val cardBg = if (isMalignant) DiagnosticMalignantBg else DiagnosticBenignBg
  val accentColor = if (isMalignant) DiagnosticMalignantRed else DiagnosticBenignGreen

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("history_record_item"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
    onClick = onItemClick
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            color = accentColor,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = if (isMalignant) "MALIGNANT RISK" else "BENIGN / NORMAL",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Text(
            text = record.confidencePercentage,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = accentColor,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = record.predictedClass,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.Black
        )

        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "$formattedDate · Model: ${record.modelName}",
          style = MaterialTheme.typography.labelSmall,
          color = Color.DarkGray
        )
      }

      IconButton(onClick = onDelete) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete record",
          tint = Color.Gray,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
