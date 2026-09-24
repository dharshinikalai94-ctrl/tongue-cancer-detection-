import React, { useState, useEffect, useRef } from "react";
import { PredictionResponse, ModelInfo } from "./types";
import "./App.css";

const API_BASE_URL = "http://localhost:8000";

export const App: React.FC = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [isAnalyzing, setIsAnalyzing] = useState<boolean>(false);
  const [prediction, setPrediction] = useState<PredictionResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [errorDetails, setErrorDetails] = useState<string | null>(null);
  const [modelInfo, setModelInfo] = useState<ModelInfo | null>(null);
  const [backendStatus, setBackendStatus] = useState<"checking" | "online" | "offline">("checking");
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Check backend health and model info on mount
  useEffect(() => {
    fetchModelInfo();
  }, []);

  const fetchModelInfo = async () => {
    try {
      setBackendStatus("checking");
      const res = await fetch(`${API_BASE_URL}/model-info`);
      if (res.ok) {
        const data: ModelInfo = await res.json();
        setModelInfo(data);
        setBackendStatus("online");
      } else {
        setBackendStatus("offline");
      }
    } catch {
      setBackendStatus("offline");
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate format (JPG, JPEG, PNG)
    const validTypes = ["image/jpeg", "image/jpg", "image/png"];
    if (!validTypes.includes(file.type)) {
      setErrorMessage("Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.");
      setErrorDetails("Invalid file format. Please upload a JPG, JPEG, or PNG tongue photograph.");
      return;
    }

    setSelectedFile(file);
    setPreviewUrl(URL.createObjectURL(file));
    setPrediction(null);
    setErrorMessage(null);
    setErrorDetails(null);
  };

  const handleClear = () => {
    setSelectedFile(null);
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(null);
    setPrediction(null);
    setErrorMessage(null);
    setErrorDetails(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleAnalyze = async () => {
    if (!selectedFile) return;

    // Check if model weights are loaded
    if (modelInfo && !modelInfo.weights_configured) {
      setErrorMessage("Prediction is unavailable because the trained EfficientNet-B0 model has not been configured.");
      setErrorDetails("The backend server is running, but no trained weights were found in backend/weights/efficientnet_b0_tongue_cancer.pt.");
      return;
    }

    setIsAnalyzing(true);
    setErrorMessage(null);
    setErrorDetails(null);
    setPrediction(null);

    const formData = new FormData();
    formData.append("file", selectedFile);

    try {
      const response = await fetch(`${API_BASE_URL}/predict`, {
        method: "POST",
        body: formData,
      });

      if (response.status === 503) {
        const err = await response.json();
        setErrorMessage("Prediction is unavailable because the trained EfficientNet-B0 model has not been configured.");
        setErrorDetails(err.detail || "Model weights have not been configured.");
      } else if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        setErrorMessage("Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.");
        setErrorDetails(err.detail || `Server responded with status code ${response.status}`);
      } else {
        const data: PredictionResponse = await response.json();
        setPrediction(data);
      }
    } catch (err: any) {
      setErrorMessage("Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection.");
      setErrorDetails(`Connection failed to ${API_BASE_URL}. Ensure FastAPI is running via 'uvicorn main:app --reload'. Error: ${err.message}`);
    } finally {
      setIsAnalyzing(false);
    }
  };

  return (
    <div className="app-container">
      {/* Navbar */}
      <header className="app-header">
        <div className="header-brand">
          <div className="brand-icon">🔬</div>
          <div>
            <h1>TongueScan AI</h1>
            <p className="subtitle">AI-Based Tongue Cancer Detection System Using EfficientNet-B0</p>
          </div>
        </div>
        <div className="status-badge-container">
          {backendStatus === "checking" && <span className="badge badge-warning">Checking Backend...</span>}
          {backendStatus === "online" && modelInfo?.weights_configured && (
            <span className="badge badge-success">● EfficientNet-B0 Active</span>
          )}
          {backendStatus === "online" && !modelInfo?.weights_configured && (
            <span className="badge badge-warning">⚠ Model Unconfigured</span>
          )}
          {backendStatus === "offline" && <span className="badge badge-danger">● Backend Offline</span>}
        </div>
      </header>

      {/* Main Content */}
      <main className="main-content">
        {/* Project Introduction */}
        <section className="intro-card">
          <h2>Project Introduction</h2>
          <p>
            Welcome to the clinical screening interface for our Final Year Project:{" "}
            <strong>AI-Based Tongue Cancer Detection System Using EfficientNet-B0</strong>. This application applies deep
            transfer learning to analyze close-up photographic examinations of oral tongue lesions, identifying textural,
            color, and architectural irregularities suggestive of Oral Squamous Cell Carcinoma (OSCC).
          </p>
          <div className="model-specs-strip">
            <div className="spec-item"><strong>Model:</strong> EfficientNet-B0</div>
            <div className="spec-item"><strong>Input Resolution:</strong> 224 × 224 RGB</div>
            <div className="spec-item"><strong>Normalization:</strong> ImageNet Mean/Std</div>
            <div className="spec-item"><strong>Inference Type:</strong> Real PyTorch Forward Pass</div>
          </div>
        </section>

        {/* Upload & Screening Area */}
        <div className="screening-grid">
          {/* Left Column: Image Upload & Preview */}
          <div className="card upload-card">
            <h3>Tongue Image Upload</h3>
            <p className="help-text">Select or drop a high-resolution photograph of the tongue mucosa (JPG, JPEG, PNG).</p>

            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept=".jpg,.jpeg,.png,image/jpeg,image/png"
              style={{ display: "none" }}
              id="tongue-file-input"
            />

            <div className="action-buttons-row">
              <label htmlFor="tongue-file-input" className="btn btn-primary">
                📁 Upload Tongue Image
              </label>
              {selectedFile && (
                <button onClick={handleClear} className="btn btn-secondary">
                  ✕ Clear
                </button>
              )}
            </div>

            {/* Preview Box */}
            <div className="preview-container">
              {previewUrl ? (
                <div className="preview-wrapper">
                  <img src={previewUrl} alt="Uploaded tongue preview" className="preview-image" />
                  <div className="image-badge">
                    {selectedFile?.name} ({Math.round((selectedFile?.size || 0) / 1024)} KB)
                  </div>
                </div>
              ) : (
                <div className="empty-preview">
                  <span className="empty-icon">📷</span>
                  <p>No image uploaded yet</p>
                  <small>Supported formats: JPG, JPEG, PNG</small>
                </div>
              )}
            </div>

            {/* Analyze Button */}
            <button
              onClick={handleAnalyze}
              disabled={!selectedFile || isAnalyzing}
              className="btn btn-accent btn-large"
            >
              {isAnalyzing ? (
                <>
                  <span className="spinner"></span> Analyzing Image with EfficientNet-B0...
                </>
              ) : (
                "⚡ Analyze Image"
              )}
            </button>
          </div>

          {/* Right Column: Prediction Results & Diagnostics */}
          <div className="card result-card">
            <h3>Prediction Results</h3>

            {/* Loading Indicator */}
            {isAnalyzing && (
              <div className="loading-state">
                <div className="pulse-loader"></div>
                <h4>Analyzing Image Pipeline</h4>
                <ol className="step-list">
                  <li>Validating image integrity and aspect ratio</li>
                  <li>Resizing to 224 × 224 RGB input tensor</li>
                  <li>Applying ImageNet standard normalization</li>
                  <li>Running feedforward pass through EfficientNet-B0</li>
                  <li>Computing Softmax class probabilities</li>
                </ol>
              </div>
            )}

            {/* Error Message Display */}
            {errorMessage && !isAnalyzing && (
              <div className="error-box">
                <div className="error-header">
                  <span className="error-icon">⚠️</span>
                  <h4>{errorMessage}</h4>
                </div>
                {errorDetails && <pre className="error-details">{errorDetails}</pre>}
              </div>
            )}

            {/* Prediction Result Display */}
            {prediction && !isAnalyzing && (
              <div className={`result-content ${prediction.is_malignant_risk ? "result-malignant" : "result-benign"}`}>
                <div className="result-header">
                  <span className="result-pill">
                    {prediction.is_malignant_risk ? "RISK IDENTIFIED" : "BENIGN / NORMAL"}
                  </span>
                  <span className="model-tag">Model: {prediction.model_name}</span>
                </div>

                <h2 className="predicted-class">{prediction.predicted_class}</h2>

                <div className="confidence-section">
                  <div className="confidence-label">
                    <span>Confidence Score:</span>
                    <strong>{prediction.confidence_percentage}</strong>
                  </div>
                  <div className="meter-track">
                    <div
                      className="meter-fill"
                      style={{ width: `${prediction.confidence_score * 100}%` }}
                    ></div>
                  </div>
                </div>

                {/* Class Probabilities Table */}
                {prediction.probabilities && (
                  <div className="probabilities-card">
                    <h5>Class Probability Distribution:</h5>
                    <ul>
                      {Object.entries(prediction.probabilities).map(([cls, prob]) => (
                        <li key={cls}>
                          <span>{cls}</span>
                          <code>{(prob * 100).toFixed(1)}%</code>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                <div className="explanation-box">
                  <h5>Result Explanation:</h5>
                  <p>{prediction.explanation}</p>
                </div>

                <small className="time-tag">Inference Latency: {prediction.inference_time_ms} ms</small>
              </div>
            )}

            {/* Empty state when no prediction and not analyzing */}
            {!prediction && !errorMessage && !isAnalyzing && (
              <div className="empty-results">
                <span className="empty-icon">📊</span>
                <p>Awaiting Image Analysis</p>
                <small>Upload a tongue image and click "Analyze Image" to receive classification from EfficientNet-B0.</small>
              </div>
            )}
          </div>
        </div>

        {/* Medical Disclaimer */}
        <section className="disclaimer-banner">
          <div className="disclaimer-icon">⚕️</div>
          <div>
            <h4>Medical Disclaimer</h4>
            <p>
              This result is intended only for preliminary screening support. It does not confirm or rule out tongue
              cancer. Please consult a qualified healthcare professional for clinical examination and further testing.
            </p>
          </div>
        </section>
      </main>

      <footer className="app-footer">
        <p>Final Year Project: AI-Based Tongue Cancer Detection System Using EfficientNet-B0</p>
      </footer>
    </div>
  );
};

export default App;
