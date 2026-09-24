export interface ModelMetrics {
  validation_accuracy?: number;
  validation_f1?: number;
  validation_auc?: number;
}

export interface ModelInfo {
  model_name: string;
  architecture: string;
  input_size: number[];
  input_normalization: string;
  weights_configured: boolean;
  weights_file: string | null;
  classes: string[];
  num_classes: number;
  metrics?: ModelMetrics;
  disclaimer: string;
}

export interface PredictionResponse {
  model_name: string;
  predicted_class: string;
  confidence_score: number;
  confidence_percentage: string;
  probabilities: Record<string, number>;
  is_malignant_risk: boolean;
  explanation: string;
  medical_disclaimer: string;
  inference_time_ms: number;
}
