package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.network.ApiClient
import com.example.network.HealthResponse
import com.example.network.ModelInfoResponse
import com.example.network.PredictionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream

sealed class ModelStatus {
  object Checking : ModelStatus()
  data class Configured(val info: ModelInfoResponse) : ModelStatus()
  data class NotConfigured(val reason: String) : ModelStatus()
  data class ConnectionError(val message: String) : ModelStatus()
}

sealed class AnalysisResult {
  data class Success(val response: PredictionResponse, val processedBitmap: Bitmap?) : AnalysisResult()
  data class ModelNotConfigured(val message: String) : AnalysisResult()
  data class Failure(val message: String, val technicalDetail: String? = null) : AnalysisResult()
}

class AnalysisRepository(private val context: Context) {

  suspend fun checkModelStatus(): ModelStatus = withContext(Dispatchers.IO) {
    try {
      val service = ApiClient.getService()
      val health = service.getHealth()
      if (!health.isSuccessful) {
        return@withContext ModelStatus.ConnectionError("Server returned HTTP ${health.code()}")
      }

      val modelInfo = service.getModelInfo()
      if (modelInfo.isSuccessful && modelInfo.body() != null) {
        val info = modelInfo.body()!!
        if (info.weightsConfigured) {
          ModelStatus.Configured(info)
        } else {
          ModelStatus.NotConfigured(
            "Prediction is unavailable because the trained EfficientNet-B0 model has not been configured."
          )
        }
      } else {
        ModelStatus.NotConfigured(
          "Prediction is unavailable because the trained EfficientNet-B0 model has not been configured."
        )
      }
    } catch (e: Exception) {
      ModelStatus.ConnectionError("Cannot connect to backend: ${e.localizedMessage}")
    }
  }

  suspend fun analyzeImage(imageUri: Uri): AnalysisResult = withContext(Dispatchers.IO) {
    try {
      // 1. Validate Image & Read Bitmap
      val inputStream = context.contentResolver.openInputStream(imageUri)
        ?: return@withContext AnalysisResult.Failure(
          "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
          "Unable to read image stream from URI: $imageUri"
        )

      val originalBitmap = BitmapFactory.decodeStream(inputStream)
      inputStream.close()

      if (originalBitmap == null) {
        return@withContext AnalysisResult.Failure(
          "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
          "Image decoding failed. Ensure the file is a valid JPG, JPEG, or PNG image."
        )
      }

      // Check format/dimensions
      if (originalBitmap.width < 10 || originalBitmap.height < 10) {
        return@withContext AnalysisResult.Failure(
          "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
          "Image resolution too low (${originalBitmap.width}x${originalBitmap.height}). Input requires clear tongue photo."
        )
      }

      // 2. Preprocessing verification: EfficientNet-B0 requires 224x224 RGB input
      val resized224 = Bitmap.createScaledBitmap(originalBitmap, 224, 224, true)
      val bos = ByteArrayOutputStream()
      resized224.compress(Bitmap.CompressFormat.JPEG, 95, bos)
      val imageBytes = bos.toByteArray()

      // 3. Check model configuration first
      val service = ApiClient.getService()
      val modelInfoCheck = try {
        service.getModelInfo()
      } catch (e: Exception) {
        null
      }

      if (modelInfoCheck == null || !modelInfoCheck.isSuccessful || modelInfoCheck.body()?.weightsConfigured == false) {
        return@withContext AnalysisResult.ModelNotConfigured(
          "Prediction is unavailable because the trained EfficientNet-B0 model has not been configured."
        )
      }

      // 4. Submit to /predict
      val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
      val multipartBody = MultipartBody.Part.createFormData("file", "tongue_input_224.jpg", requestBody)

      val response = service.predict(multipartBody)
      if (response.isSuccessful && response.body() != null) {
        val prediction = response.body()!!
        AnalysisResult.Success(prediction, resized224)
      } else {
        val errBody = response.errorBody()?.string()
        AnalysisResult.Failure(
          "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
          "HTTP ${response.code()} from /predict: $errBody"
        )
      }
    } catch (e: java.net.ConnectException) {
      AnalysisResult.Failure(
        "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
        "Connection refused to ${ApiClient.getBaseUrl()}. Ensure FastAPI backend is running (uvicorn main:app --reload --host 0.0.0.0 --port 8000)."
      )
    } catch (e: Exception) {
      AnalysisResult.Failure(
        "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
        "Exception during inference: ${e.message}"
      )
    }
  }

  suspend fun analyzeBitmap(bitmap: Bitmap): AnalysisResult = withContext(Dispatchers.IO) {
    try {
      val resized224 = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
      val bos = ByteArrayOutputStream()
      resized224.compress(Bitmap.CompressFormat.JPEG, 95, bos)
      val imageBytes = bos.toByteArray()

      val service = ApiClient.getService()
      val modelInfoCheck = try {
        service.getModelInfo()
      } catch (e: Exception) {
        null
      }

      if (modelInfoCheck == null || !modelInfoCheck.isSuccessful || modelInfoCheck.body()?.weightsConfigured == false) {
        return@withContext AnalysisResult.ModelNotConfigured(
          "Prediction is unavailable because the trained EfficientNet-B0 model has not been configured."
        )
      }

      val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
      val multipartBody = MultipartBody.Part.createFormData("file", "tongue_input_224.jpg", requestBody)

      val response = service.predict(multipartBody)
      if (response.isSuccessful && response.body() != null) {
        AnalysisResult.Success(response.body()!!, resized224)
      } else {
        val errBody = response.errorBody()?.string()
        AnalysisResult.Failure(
          "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
          "HTTP ${response.code()}: $errBody"
        )
      }
    } catch (e: Exception) {
      AnalysisResult.Failure(
        "Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.",
        e.message
      )
    }
  }
}
