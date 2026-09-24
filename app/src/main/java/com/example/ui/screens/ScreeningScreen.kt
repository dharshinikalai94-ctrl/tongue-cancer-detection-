package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ModelStatus
import com.example.ui.ScreenTab
import com.example.ui.UiState
import com.example.ui.theme.DiagnosticBenignBg
import com.example.ui.theme.DiagnosticBenignGreen
import com.example.ui.theme.DiagnosticMalignantBg
import com.example.ui.theme.DiagnosticMalignantRed
import com.example.ui.theme.DiagnosticWarningAmber
import com.example.ui.theme.DiagnosticWarningBg

@Composable
fun ScreeningScreen(
  uiState: UiState,
  onImageSelected: (android.net.Uri) -> Unit,
  onSampleSelected: (String) -> Unit,
  onClear: () -> Unit,
  onAnalyze: () -> Unit,
  onNavigateToConfig: () -> Unit,
  onNavigateToGuide: () -> Unit
) {
  val scrollState = rememberScrollState()

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      onImageSelected(uri)
    }
  }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      // Handle camera bitmap
      onSampleSelected("camera")
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Project Banner Card
    OutlinedCard(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Science,
              contentDescription = "Project Icon",
              tint = MaterialTheme.colorScheme.primary
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "FINAL YEAR PROJECT",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Text(
              text = "Tongue Cancer Detection System",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Deep learning screening system utilizing an EfficientNet-B0 convolutional neural network architecture for preliminary tongue lesion classification.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))
        // Model Status Pill
        ModelStatusPill(
          status = uiState.modelStatus,
          onNavigateToConfig = onNavigateToConfig
        )
      }
    }

    // Image Upload & Selection Section
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Tongue Image Input",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "JPG, JPEG, PNG",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Upload Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier
              .weight(1f)
              .testTag("upload_image_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Image")
          }

          OutlinedButton(
            onClick = {
              cameraLauncher.launch(null)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("camera_button")
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = "Take Photo",
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Test Evaluation Samples (Quick selector for viva / examiners / testing without local files)
        Text(
          text = "Quick Evaluation Test Samples:",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilledTonalButton(
            onClick = { onSampleSelected("normal") },
            modifier = Modifier
              .weight(1f)
              .testTag("sample_normal_button"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
          ) {
            Text("Sample Normal Mucosa", fontSize = 11.sp, textAlign = TextAlign.Center)
          }

          FilledTonalButton(
            onClick = { onSampleSelected("lesion") },
            modifier = Modifier
              .weight(1f)
              .testTag("sample_lesion_button"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
          ) {
            Text("Sample Tongue Lesion", fontSize = 11.sp, textAlign = TextAlign.Center)
          }
        }

        // Image Preview Box
        if (uiState.selectedBitmap != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black.copy(alpha = 0.05f))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Image(
              bitmap = uiState.selectedBitmap.asImageBitmap(),
              contentDescription = "Uploaded Tongue Image Preview",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit
            )

            // Image Metadata overlay
            Surface(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
              color = Color.Black.copy(alpha = 0.65f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "${uiState.selectedImageName ?: "Tongue Image"} | Resized to 224x224 RGB",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontFamily = FontFamily.Monospace
              )
            }
          }
        } else {
          // Empty State Prompt
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
              )
              Text(
                text = "No image selected",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Select a JPG, JPEG, or PNG tongue image to begin screening",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }
        }

        // Action Buttons: Analyze & Clear
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onAnalyze,
            enabled = uiState.selectedBitmap != null && !uiState.isAnalyzing,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("analyze_image_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            if (uiState.isAnalyzing) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Analyzing...")
            } else {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Analyze Image")
            }
          }

          OutlinedButton(
            onClick = onClear,
            enabled = uiState.selectedBitmap != null && !uiState.isAnalyzing,
            modifier = Modifier
              .height(48.dp)
              .testTag("clear_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Clear",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear")
          }
        }
      }
    }

    // Loading Step Indicator
    AnimatedVisibility(
      visible = uiState.isAnalyzing,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically()
    ) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.5.dp,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "EfficientNet-B0 Analysis in Progress",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          LinearProgressIndicator(
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary
          )

          Text(
            text = uiState.analysisStep,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Error Message Card: Check for exact required strings
    if (uiState.isModelNotConfigured) {
      ModelNotConfiguredCard(onNavigateToConfig = onNavigateToConfig)
    } else if (uiState.errorMessage != null) {
      PredictionFailedCard(
        errorMessage = uiState.errorMessage,
        errorDetail = uiState.errorDetail,
        onNavigateToConfig = onNavigateToConfig
      )
    }

    // Prediction Result Card
    if (uiState.prediction != null) {
      PredictionResultCard(
        prediction = uiState.prediction,
        analyzedBitmap = uiState.processedBitmap ?: uiState.selectedBitmap
      )
    }

    // Mandatory Medical Disclaimer
    MedicalDisclaimerCard()
  }
}

@Composable
fun ModelStatusPill(
  status: ModelStatus,
  onNavigateToConfig: () -> Unit
) {
  val (bgColor, textColor, icon, label) = when (status) {
    is ModelStatus.Configured -> Quadruple(
      DiagnosticBenignBg,
      DiagnosticBenignGreen,
      Icons.Default.CloudDone,
      "EfficientNet-B0 Model: Active & Ready"
    )
    is ModelStatus.NotConfigured -> Quadruple(
      DiagnosticWarningBg,
      DiagnosticWarningAmber,
      Icons.Default.Warning,
      "Trained Model Not Configured"
    )
    is ModelStatus.ConnectionError -> Quadruple(
      DiagnosticMalignantBg,
      DiagnosticMalignantRed,
      Icons.Default.CloudOff,
      "Backend Offline (${status.message.take(28)}...)"
    )
    is ModelStatus.Checking -> Quadruple(
      MaterialTheme.colorScheme.surfaceVariant,
      MaterialTheme.colorScheme.onSurfaceVariant,
      Icons.Default.Info,
      "Checking Model Configuration..."
    )
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable { onNavigateToConfig() },
    color = bgColor,
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = textColor,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = label,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = textColor
        )
      }
      Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Config",
        tint = textColor,
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ModelNotConfiguredCard(onNavigateToConfig: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DiagnosticWarningBg),
    border = BorderStroke(1.dp, DiagnosticWarningAmber.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Warning",
          tint = DiagnosticWarningAmber
        )
        Text(
          text = "Configuration Required",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = DiagnosticWarningAmber
        )
      }

      // Exact prompt text requirement:
      Text(
        text = "Prediction is unavailable because the trained EfficientNet-B0 model has not been configured.",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF6E4500)
      )

      Text(
        text = "To run real deep learning inference, start the Python FastAPI backend with your trained weights in backend/weights/efficientnet_b0_tongue_cancer.pt or configure the model settings.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF8A5A00)
      )

      Button(
        onClick = onNavigateToConfig,
        colors = ButtonDefaults.buttonColors(containerColor = DiagnosticWarningAmber),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.align(Alignment.End)
      ) {
        Text("Configure Model & Backend", fontSize = 12.sp)
      }
    }
  }
}

@Composable
fun PredictionFailedCard(
  errorMessage: String,
  errorDetail: String?,
  onNavigateToConfig: () -> Unit
) {
  var showDetails by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DiagnosticMalignantBg),
    border = BorderStroke(1.dp, DiagnosticMalignantRed.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.ErrorOutline,
          contentDescription = "Error",
          tint = DiagnosticMalignantRed
        )
        Text(
          text = "Screening Error",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = DiagnosticMalignantRed
        )
      }

      // Exact prompt text requirement:
      Text(
        text = "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = DiagnosticMalignantRed
      )

      if (errorDetail != null) {
        Text(
          text = if (showDetails) "Hide Technical Diagnostic" else "Show Diagnostic Details",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { showDetails = !showDetails }
        )

        if (showDetails) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.8f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = errorDetail,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = Color.DarkGray,
              modifier = Modifier.padding(8.dp)
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        OutlinedButton(
          onClick = onNavigateToConfig,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Backend Connection Settings", fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun PredictionResultCard(
  prediction: com.example.network.PredictionResponse,
  analyzedBitmap: Bitmap?
) {
  val isMalignant = prediction.isMalignantRisk ?: (
    prediction.predictedClass.contains("Carcinoma", ignoreCase = true) ||
      prediction.predictedClass.contains("Malignant", ignoreCase = true)
  )

  val cardBg = if (isMalignant) DiagnosticMalignantBg else DiagnosticBenignBg
  val cardAccent = if (isMalignant) DiagnosticMalignantRed else DiagnosticBenignGreen
  val labelText = if (isMalignant) "POTENTIAL MALIGNANCY DETECTED" else "NO MALIGNANT FEATURES DETECTED"

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("prediction_result_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = BorderStroke(1.5.dp, cardAccent.copy(alpha = 0.8f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header with Model Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = cardAccent,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = labelText,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            letterSpacing = 0.5.sp
          )
        }

        Surface(
          color = Color.White.copy(alpha = 0.9f),
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, cardAccent.copy(alpha = 0.3f))
        ) {
          Text(
            text = "Model: ${prediction.modelName}",
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      // Predicted Class
      Text(
        text = prediction.predictedClass,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = cardAccent
      )

      // Confidence Score with Visual Meter
      val confidence = prediction.confidenceScore
      val confidencePct = prediction.confidencePercentage ?: String.format("%.1f%%", confidence * 100)

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Confidence Score",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.75f)
          )
          Text(
            text = confidencePct,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = cardAccent
          )
        }

        LinearProgressIndicator(
          progress = { confidence },
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp)),
          color = cardAccent,
          trackColor = Color.White.copy(alpha = 0.7f)
        )
      }

      // Probability Distribution Table (if available)
      if (!prediction.probabilities.isNullOrEmpty()) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = Color.White.copy(alpha = 0.85f),
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Class Probability Distribution (EfficientNet-B0):",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color.DarkGray
            )

            prediction.probabilities.forEach { (className, prob) ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = className,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.Black
                )
                Text(
                  text = String.format("%.1f%%", prob * 100),
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }

      // Clinical Explanation
      if (!prediction.explanation.isNullOrBlank()) {
        Text(
          text = "Clinical Feature Analysis:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = Color.Black.copy(alpha = 0.8f)
        )
        Text(
          text = prediction.explanation,
          style = MaterialTheme.typography.bodyMedium,
          color = Color.Black.copy(alpha = 0.85f),
          lineHeight = 20.sp
        )
      }

      if (prediction.inferenceTimeMs != null) {
        Text(
          text = "Inference time: ${prediction.inferenceTimeMs} ms",
          style = MaterialTheme.typography.labelSmall,
          color = Color.Gray,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
fun MedicalDisclaimerCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.Top
    ) {
      Icon(
        imageVector = Icons.Default.HealthAndSafety,
        contentDescription = "Medical Disclaimer",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
      )

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Medical Disclaimer",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Exact disclaimer requirement:
        Text(
          text = "This result is intended only for preliminary screening support. It does not confirm or rule out tongue cancer. Please consult a qualified healthcare professional for clinical examination and further testing.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 18.sp
        )
      }
    }
  }
}
