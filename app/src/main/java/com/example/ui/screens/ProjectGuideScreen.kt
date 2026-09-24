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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProjectGuideScreen() {
  val scrollState = rememberScrollState()

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
          imageVector = Icons.Default.MenuBook,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
      }
      Column {
        Text(
          text = "Project & Training Guide",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Academic methodology and EfficientNet-B0 implementation",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Section 1: Tongue vs Oral Cancer Dataset Notice
    GuideSectionCard(
      title = "1. Tongue-Specific Dataset Preparation",
      icon = Icons.Default.Dataset,
      accentColor = MaterialTheme.colorScheme.primary
    ) {
      Text(
        text = "Clinical Differentiation Crucial Rule:",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
      )
      Text(
        text = "Do not assume that an oral cancer dataset is automatically a tongue cancer dataset. General oral cancer datasets frequently mix lesions from buccal mucosa, gingiva, floor of the mouth, hard palate, and retromolar trigone.",
        style = MaterialTheme.typography.bodySmall,
        lineHeight = 18.sp
      )
      Text(
        text = "Tongue Squamous Cell Carcinoma (TSCC) primarily originates on the lateral borders (75%) and anterior two-thirds of the ventral surface. Training images must specifically capture:",
        style = MaterialTheme.typography.bodySmall
      )
      BulletPoint("Class 0 (Benign / Normal): Healthy dorsal filiform/fungiform papillae, benign geographic tongue, or recurrent aphthous ulcers.")
      BulletPoint("Class 1 (Malignant / OSCC): Exophytic masses, endophytic ulcerated lesions with indurated margins, or erythroplakic malignant transformations.")
      BulletPoint("Resolution & Lighting: Consistent clinical white-light photography without glare or saliva reflection artifacts.")
    }

    // Section 2: Data Splitting, Resizing & Augmentation
    GuideSectionCard(
      title = "2. Dataset Split & Preprocessing Pipeline",
      icon = Icons.Default.Biotech,
      accentColor = Color(0xFF00796B)
    ) {
      BulletPoint("Dataset Split: 80% Training, 10% Validation, 10% Independent Test split stratified by patient to prevent data leakage.")
      BulletPoint("Image Input Dimensions: 224 x 224 pixels RGB (native EfficientNet-B0 input tensor shape: [batch_size, 3, 224, 224]).")
      BulletPoint("Input Normalization: Standard ImageNet mean [0.485, 0.456, 0.406] and std [0.229, 0.224, 0.225].")
      BulletPoint("Data Augmentation: Random horizontal/vertical flips, subtle rotation (±15°), random color jitter (brightness 0.1, contrast 0.1) to account for diverse oral examination camera conditions.")
    }

    // Section 3: EfficientNet-B0 Architecture & Transfer Learning
    GuideSectionCard(
      title = "3. EfficientNet-B0 Transfer Learning Strategy",
      icon = Icons.Default.ModelTraining,
      accentColor = Color(0xFF1565C0)
    ) {
      Text(
        text = "Why EfficientNet-B0?",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge
      )
      Text(
        text = "EfficientNet uses compound scaling (balancing depth d, width w, and image resolution r). EfficientNet-B0 provides 5.3M parameters, achieving high diagnostic accuracy while running efficiently on edge devices and clinical workstations.",
        style = MaterialTheme.typography.bodySmall
      )
      Spacer(modifier = Modifier.height(4.dp))
      BulletPoint("Phase 1 (Feature Extraction): Freeze pre-trained ImageNet backbone. Train new classification head (Dropout 0.3 + Linear/Dense to 2 classes) for 10 epochs with Adam optimizer (lr = 1e-3).")
      BulletPoint("Phase 2 (Fine-Tuning): Unfreeze top MBConv stages (stages 6 & 7). Train with cosine decay learning rate (lr = 1e-4) for 25 epochs.")
      BulletPoint("Loss Function: Binary Cross-Entropy with Logits or Weighted Cross-Entropy to handle class imbalance.")
    }

    // Section 4: Evaluation Metrics & Academic Rigor
    GuideSectionCard(
      title = "4. Evaluation Metrics & Clinical Integrity",
      icon = Icons.Default.Security,
      accentColor = Color(0xFF4527A0)
    ) {
      BulletPoint("Primary Metrics: Sensitivity (Recall of malignant cases), Specificity (True negative rate), Precision, F1-Score, and Area Under ROC Curve (AUC-ROC).")
      BulletPoint("Clinical Priority: Minimizing False Negatives (high recall) is critical in cancer screening so malignant cases are never missed.")
      BulletPoint("Realistic Reporting: Never claim 100% accuracy. Clinical screening AI models typically achieve 88%–96% sensitivity depending on biopsy ground truth.")
      BulletPoint("Confusion Matrix: Include full confusion matrix in final year project thesis.")
    }

    // Section 5: Files & Execution Instructions
    GuideSectionCard(
      title = "5. Backend and Training Scripts Included",
      icon = Icons.Default.CollectionsBookmark,
      accentColor = MaterialTheme.colorScheme.primary
    ) {
      Text(
        text = "The complete runnable scripts are included in this project:",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold
      )
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("• backend/main.py (FastAPI with /health, /model-info, /predict)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
          Text("• backend/train_efficientnet_b0.py (Complete PyTorch training script)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
          Text("• backend/class_mapping.json (Standard labels)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
          Text("• backend/requirements.txt (Dependencies)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
          Text("• frontend-web/ (React + TypeScript web application)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
      }
    }

    // Disclaimer
    MedicalDisclaimerCard()
  }
}

@Composable
fun GuideSectionCard(
  title: String,
  icon: ImageVector,
  accentColor: Color,
  content: @Composable () -> Unit
) {
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
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(22.dp)
        )
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      content()
    }
  }
}

@Composable
fun BulletPoint(text: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 18.sp
    )
  }
}
