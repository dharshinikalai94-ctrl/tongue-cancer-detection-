package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponse(
  @Json(name = "status") val status: String,
  @Json(name = "service") val service: String? = null,
  @Json(name = "model_loaded") val modelLoaded: Boolean = false,
  @Json(name = "timestamp") val timestamp: String? = null
)

@JsonClass(generateAdapter = true)
data class ModelMetrics(
  @Json(name = "validation_accuracy") val validationAccuracy: Double? = null,
  @Json(name = "validation_f1") val validationF1: Double? = null,
  @Json(name = "validation_auc") val validationAuc: Double? = null
)

@JsonClass(generateAdapter = true)
data class ModelInfoResponse(
  @Json(name = "model_name") val modelName: String = "EfficientNet-B0",
  @Json(name = "architecture") val architecture: String? = "EfficientNet-B0",
  @Json(name = "input_size") val inputSize: List<Int>? = listOf(224, 224, 3),
  @Json(name = "input_normalization") val inputNormalization: String? = "Mean [0.485, 0.456, 0.406], Std [0.229, 0.224, 0.225]",
  @Json(name = "weights_configured") val weightsConfigured: Boolean = false,
  @Json(name = "weights_file") val weightsFile: String? = null,
  @Json(name = "classes") val classes: List<String>? = listOf("Benign / Normal", "Squamous Cell Carcinoma (Malignant)"),
  @Json(name = "num_classes") val numClasses: Int? = 2,
  @Json(name = "metrics") val metrics: ModelMetrics? = null,
  @Json(name = "disclaimer") val disclaimer: String? = null
)

@JsonClass(generateAdapter = true)
data class PredictionResponse(
  @Json(name = "model_name") val modelName: String = "EfficientNet-B0",
  @Json(name = "predicted_class") val predictedClass: String,
  @Json(name = "confidence_score") val confidenceScore: Float,
  @Json(name = "confidence_percentage") val confidencePercentage: String? = null,
  @Json(name = "probabilities") val probabilities: Map<String, Float>? = null,
  @Json(name = "is_malignant_risk") val isMalignantRisk: Boolean? = false,
  @Json(name = "explanation") val explanation: String? = null,
  @Json(name = "medical_disclaimer") val medicalDisclaimer: String? = null,
  @Json(name = "inference_time_ms") val inferenceTimeMs: Long? = null
)
