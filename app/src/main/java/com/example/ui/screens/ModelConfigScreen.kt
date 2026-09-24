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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.ModelStatus
import com.example.ui.UiState
import com.example.ui.theme.DiagnosticBenignBg
import com.example.ui.theme.DiagnosticBenignGreen
import com.example.ui.theme.DiagnosticWarningAmber
import com.example.ui.theme.DiagnosticWarningBg

@Composable
fun ModelConfigScreen(
  uiState: UiState,
  onUpdateBackendUrl: (String) -> Unit,
  onRefreshStatus: () -> Unit
) {
  val scrollState = rememberScrollState()
  var inputUrl by remember(uiState.backendUrl) { mutableStateOf(uiState.backendUrl) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Tune,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
      }
      Column {
        Text(
          text = "Model & Backend Configuration",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "EfficientNet-B0 inference server and weights setup",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Backend Connection Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Python FastAPI Backend URL",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
          value = inputUrl,
          onValueChange = { inputUrl = it },
          label = { Text("Server Base URL") },
          placeholder = { Text("http://10.0.2.2:8000/") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("backend_url_input"),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        // Presets for quick setup
        Text(
          text = "Quick Presets:",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilledTonalButton(
            onClick = {
              inputUrl = "http://10.0.2.2:8000/"
              onUpdateBackendUrl("http://10.0.2.2:8000/")
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Emulator Host (10.0.2.2)", fontSize = 10.sp)
          }

          FilledTonalButton(
            onClick = {
              inputUrl = "http://localhost:8000/"
              onUpdateBackendUrl("http://localhost:8000/")
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Localhost", fontSize = 10.sp)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = { onUpdateBackendUrl(inputUrl) },
            modifier = Modifier
              .weight(1f)
              .testTag("save_and_test_backend_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CloudSync,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save & Connect")
          }

          Button(
            onClick = onRefreshStatus,
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh",
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // Live Model Status Details
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Model Specifications (EfficientNet-B0)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        SpecRow(label = "Model Architecture", value = "EfficientNet-B0 (Transfer Learning)")
        SpecRow(label = "Required Input Size", value = "224 x 224 x 3 RGB")
        SpecRow(label = "Preprocessing", value = "ImageNet normalization (mean / std)")
        SpecRow(label = "Output Activation", value = "Softmax probability distribution")
        SpecRow(label = "Class Index 0", value = "Benign / Normal Tongue Tissue")
        SpecRow(label = "Class Index 1", value = "Squamous Cell Carcinoma (OSCC / Malignant)")

        Spacer(modifier = Modifier.height(4.dp))
        when (val status = uiState.modelStatus) {
          is ModelStatus.Configured -> {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              color = DiagnosticBenignBg,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, DiagnosticBenignGreen.copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DiagnosticBenignGreen, modifier = Modifier.size(16.dp))
                  Text("Trained Weights Loaded Successfully", fontWeight = FontWeight.Bold, color = DiagnosticBenignGreen, fontSize = 12.sp)
                }
                Text("Weights file: ${status.info.weightsFile ?: "efficientnet_b0_tongue_cancer.pt"}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                status.info.metrics?.let {
                  Text("Validation Accuracy: ${String.format("%.2f%%", (it.validationAccuracy ?: 0.0) * 100)} | AUC: ${it.validationAuc ?: 0.0}", fontSize = 11.sp)
                }
              }
            }
          }
          is ModelStatus.NotConfigured -> {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              color = DiagnosticWarningBg,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, DiagnosticWarningAmber.copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = DiagnosticWarningAmber, modifier = Modifier.size(16.dp))
                  Text("Model Weights Not Loaded", fontWeight = FontWeight.Bold, color = DiagnosticWarningAmber, fontSize = 12.sp)
                }
                Text(
                  "Prediction is unavailable because the trained EfficientNet-B0 model has not been configured.",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp,
                  color = Color(0xFF6E4500)
                )
              }
            }
          }
          is ModelStatus.ConnectionError -> {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.errorContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Cannot connect to backend server at ${uiState.backendUrl}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                Text(status.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
              }
            }
          }
          is ModelStatus.Checking -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Text("Querying /model-info endpoint...", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Step-by-Step Instructions Card: "Where I need to configure my trained EfficientNet-B0 model"
    OutlinedCard(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text(
            text = "Where to Configure Trained Weights",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Text(
          text = "Follow these 3 exact steps in the project repository:",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold
        )

        InstructionStep(
          stepNumber = "1",
          title = "Train or place weights file",
          description = "Place your trained model weights in: \n`backend/weights/efficientnet_b0_tongue_cancer.pt` (PyTorch) or `model.keras` (TensorFlow/Keras)."
        )

        InstructionStep(
          stepNumber = "2",
          title = "Check class_mapping.json",
          description = "Ensure `backend/class_mapping.json` matches your training labels:\n{\n  \"0\": \"Benign / Normal Tongue Tissue\",\n  \"1\": \"Squamous Cell Carcinoma (OSCC / Malignant)\"\n}"
        )

        InstructionStep(
          stepNumber = "3",
          title = "Run FastAPI Backend",
          description = "Run the server via terminal:\n`cd backend && uvicorn main:app --reload --host 0.0.0.0 --port 8000`\nThe app will detect the server at http://10.0.2.2:8000 (emulator) or your local network IP."
        )
      }
    }
  }
}

@Composable
fun SpecRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun InstructionStep(stepNumber: String, title: String, description: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(6.dp),
      color = MaterialTheme.colorScheme.primaryContainer,
      modifier = Modifier.size(24.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = stepNumber,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = description,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(6.dp)
        )
      }
    }
  }
}
