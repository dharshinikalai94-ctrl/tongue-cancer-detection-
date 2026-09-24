package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AnalysisRepository
import com.example.data.AnalysisResult
import com.example.data.AppDatabase
import com.example.data.ModelStatus
import com.example.data.ScreeningRecord
import com.example.data.ScreeningRepository
import com.example.network.ApiClient
import com.example.network.PredictionResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

enum class ScreenTab {
  SCREENING,
  HISTORY,
  MODEL_CONFIG,
  GUIDE
}

data class UiState(
  val selectedTab: ScreenTab = ScreenTab.SCREENING,
  val selectedImageUri: Uri? = null,
  val selectedBitmap: Bitmap? = null,
  val selectedImageName: String? = null,
  val imageDimensions: Pair<Int, Int>? = null,
  val isAnalyzing: Boolean = false,
  val analysisStep: String = "",
  val prediction: PredictionResponse? = null,
  val processedBitmap: Bitmap? = null,
  val isModelNotConfigured: Boolean = false,
  val errorMessage: String? = null,
  val errorDetail: String? = null,
  val modelStatus: ModelStatus = ModelStatus.Checking,
  val backendUrl: String = ApiClient.getBaseUrl()
)

class TongueScanViewModel(application: Application) : AndroidViewModel(application) {
  private val analysisRepository = AnalysisRepository(application.applicationContext)
  private val screeningRepository: ScreeningRepository

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  val historyList: StateFlow<List<ScreeningRecord>>

  init {
    val database = AppDatabase.getDatabase(application.applicationContext)
    screeningRepository = ScreeningRepository(database.screeningDao())
    historyList = screeningRepository.allScreenings.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )
    checkBackendModelStatus()
  }

  fun setTab(tab: ScreenTab) {
    _uiState.value = _uiState.value.copy(selectedTab = tab)
  }

  fun checkBackendModelStatus() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(modelStatus = ModelStatus.Checking)
      val status = analysisRepository.checkModelStatus()
      _uiState.value = _uiState.value.copy(
        modelStatus = status,
        isModelNotConfigured = status is ModelStatus.NotConfigured
      )
    }
  }

  fun updateBackendUrl(newUrl: String) {
    ApiClient.updateBaseUrl(newUrl)
    _uiState.value = _uiState.value.copy(backendUrl = ApiClient.getBaseUrl())
    checkBackendModelStatus()
  }

  fun onImageSelected(uri: Uri) {
    viewModelScope.launch {
      try {
        val context = getApplication<Application>().applicationContext
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap != null) {
          _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            selectedBitmap = bitmap,
            selectedImageName = "Uploaded Image (${bitmap.width}x${bitmap.height})",
            imageDimensions = Pair(bitmap.width, bitmap.height),
            prediction = null,
            errorMessage = null,
            errorDetail = null,
            isModelNotConfigured = false
          )
        } else {
          _uiState.value = _uiState.value.copy(
            errorMessage = "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
            errorDetail = "Unsupported image format. Please select a valid JPG, JPEG, or PNG tongue image."
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          errorMessage = "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
          errorDetail = "Failed to load selected image: ${e.message}"
        )
      }
    }
  }

  fun onSampleSelected(type: String) {
    viewModelScope.launch {
      val context = getApplication<Application>().applicationContext
      val resId = if (type == "normal") R.drawable.img_sample_normal else R.drawable.img_medical_hero
      val bitmap = BitmapFactory.decodeResource(context.resources, resId)
      if (bitmap != null) {
        _uiState.value = _uiState.value.copy(
          selectedImageUri = null,
          selectedBitmap = bitmap,
          selectedImageName = if (type == "normal") "Clinical Sample: Normal Dorsal Tongue" else "Clinical Sample: Reference Tongue Lesion",
          imageDimensions = Pair(bitmap.width, bitmap.height),
          prediction = null,
          errorMessage = null,
          errorDetail = null,
          isModelNotConfigured = false
        )
      }
    }
  }

  fun clearSelection() {
    _uiState.value = _uiState.value.copy(
      selectedImageUri = null,
      selectedBitmap = null,
      selectedImageName = null,
      imageDimensions = null,
      prediction = null,
      processedBitmap = null,
      isAnalyzing = false,
      analysisStep = "",
      errorMessage = null,
      errorDetail = null,
      isModelNotConfigured = false
    )
  }

  fun analyzeImage() {
    val bitmap = _uiState.value.selectedBitmap ?: return
    val imageUri = _uiState.value.selectedImageUri

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(
        isAnalyzing = true,
        analysisStep = "1/4 Validating JPG/PNG input dimensions...",
        errorMessage = null,
        errorDetail = null,
        isModelNotConfigured = false
      )

      delay(300)
      _uiState.value = _uiState.value.copy(
        analysisStep = "2/4 Preprocessing image to 224x224 RGB tensor with EfficientNet normalization..."
      )

      delay(300)
      _uiState.value = _uiState.value.copy(
        analysisStep = "3/4 Running inference through trained EfficientNet-B0 network..."
      )

      val result = if (imageUri != null) {
        analysisRepository.analyzeImage(imageUri)
      } else {
        analysisRepository.analyzeBitmap(bitmap)
      }

      when (result) {
        is AnalysisResult.Success -> {
          _uiState.value = _uiState.value.copy(
            isAnalyzing = false,
            analysisStep = "4/4 Computing softmax probabilities and class confidence.",
            prediction = result.response,
            processedBitmap = result.processedBitmap
          )
          // Save to local Room database
          val isMalignant = result.response.isMalignantRisk ?: (result.response.predictedClass.contains("Carcinoma", ignoreCase = true) || result.response.predictedClass.contains("Malignant", ignoreCase = true))
          val record = ScreeningRecord(
            imageUri = imageUri?.toString() ?: "sample://clinical_reference",
            modelName = result.response.modelName,
            predictedClass = result.response.predictedClass,
            confidenceScore = result.response.confidenceScore,
            confidencePercentage = result.response.confidencePercentage ?: String.format("%.1f%%", result.response.confidenceScore * 100),
            explanation = result.response.explanation ?: "Deep learning feature analysis using EfficientNet-B0 backbone.",
            isMalignantRisk = isMalignant,
            backendSource = ApiClient.getBaseUrl()
          )
          screeningRepository.insertScreening(record)
        }

        is AnalysisResult.ModelNotConfigured -> {
          _uiState.value = _uiState.value.copy(
            isAnalyzing = false,
            isModelNotConfigured = true,
            errorMessage = result.message,
            errorDetail = "The EfficientNet-B0 model weights file has not been loaded on the backend or on-device model asset is missing. Please configure your trained weights in backend/weights/."
          )
        }

        is AnalysisResult.Failure -> {
          _uiState.value = _uiState.value.copy(
            isAnalyzing = false,
            errorMessage = result.message,
            errorDetail = result.technicalDetail ?: "Connection or preprocessing pipeline failed."
          )
        }
      }
    }
  }

  fun deleteHistory(id: Long) {
    viewModelScope.launch {
      screeningRepository.deleteScreening(id)
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      screeningRepository.clearAll()
    }
  }
}
