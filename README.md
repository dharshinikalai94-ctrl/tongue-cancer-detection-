# AI-Based Tongue Cancer Detection System Using EfficientNet-B0

> **Final Year Project**  
> **Clinical Screening Support System**  
> *Undergraduate / Postgraduate Engineering & Applied Medical AI Thesis*

---

## 1. Project Overview & Clinical Objective
Tongue Squamous Cell Carcinoma (TSCC) is an aggressive malignancy of the head and neck. Early detection dramatically improves patient survival from less than 40% to over 85%. However, early-stage tongue cancer frequently mimics benign mucosal conditions (aphthous stomatitis, geographic tongue, frictional keratosis, or traumatic ulcers).

This project implements an end-to-end deep learning screening system utilizing **EfficientNet-B0**, an advanced Convolutional Neural Network (CNN) architecture optimized via compound scaling.

The system comprises:
1. **Android Mobile App (`/app`)**: Built with Kotlin and Jetpack Compose for point-of-care mobile screening, local Room database for examination logs, and direct integration with the inference engine.
2. **Python FastAPI Backend (`/backend`)**: High-performance REST API handling image validation, 224×224 tensor preprocessing, EfficientNet-B0 inference, and probability calibration.
3. **Web Frontend (`/frontend-web`)**: Modern React + TypeScript clinical dashboard for desktop and hospital workstations.
4. **Model Training Pipeline (`/backend/train_efficientnet_b0.py`)**: Two-stage transfer learning pipeline with data augmentation, learning rate scheduling, and comprehensive evaluation metrics.

---

## 2. System Architecture & Flowchart

```
┌────────────────────────────────────────────────────────┐
│              Tongue Image Input (JPG/PNG)              │
│    (Clinical examination photo of dorsal / lateral)    │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                   Input Validation                     │
│  - File format verification (JPG/JPEG/PNG)             │
│  - Resolution check (min 10x10 px)                     │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                 Image Preprocessing                    │
│  - Bilinear Resize to 224 × 224 RGB                    │
│  - Pixel normalization: (pixel - mean) / std           │
│    Mean: [0.485, 0.456, 0.406]                         │
│    Std:  [0.229, 0.224, 0.225]                         │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                EfficientNet-B0 Model                   │
│  - Pre-trained ImageNet backbone (Compound Scaling)    │
│  - MBConv (Mobile Inverted Bottleneck Conv) blocks     │
│  - Squeeze-and-Excitation attention mechanisms         │
│  - Custom Classification Head:                         │
│      Dropout(p=0.3) -> Linear(1280, 2)                 │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                Softmax & Class Mapping                 │
│  - Class 0: Benign / Normal Tongue Tissue              │
│  - Class 1: Squamous Cell Carcinoma (OSCC / Malignant) │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│             Output Prediction & UI Display             │
│  - Predicted Class Label & Visual Confidence Meter     │
│  - Detailed Clinical Morphological Explanation         │
│  - Prominent Medical Disclaimer                        │
└────────────────────────────────────────────────────────┘
```

---

## 3. Repository Directory Structure

```text
├── app/                                 # Native Android Application (Kotlin + Jetpack Compose)
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt              # App navigation & edge-to-edge scaffolding
│   │   ├── data/                        # Room Database & Analysis Repository
│   │   │   ├── AppDatabase.kt           # Room Database definition
│   │   │   ├── ScreeningRecord.kt       # Room Entity for past screenings
│   │   │   ├── ScreeningDao.kt          # DAO for persistence
│   │   │   ├── ScreeningRepository.kt   # Local database repository
│   │   │   └── AnalysisRepository.kt    # Preprocessing, validation & API logic
│   │   ├── network/                     # Retrofit2 REST Client
│   │   │   ├── ApiClient.kt             # Dynamic BaseURL manager & OkHttp
│   │   │   ├── ApiModels.kt             # Moshi JSON data structures
│   │   │   └── TongueDetectionApiService.kt
│   │   └── ui/
│   │       ├── TongueScanViewModel.kt   # MVVM StateFlow manager
│   │       ├── screens/
│   │       │   ├── ScreeningScreen.kt   # Image upload, preview, progress & result card
│   │       │   ├── HistoryScreen.kt     # Saved screening records list
│   │       │   ├── ModelConfigScreen.kt # Backend URL, weights status & guide
│   │       │   └── ProjectGuideScreen.kt# Full academic dataset & training thesis guide
│   │       └── theme/                   # Material 3 Clinical Healthcare Theme
│   └── build.gradle.kts
│
├── backend/                             # Python FastAPI Inference Engine & Training
│   ├── main.py                          # FastAPI with /health, /model-info, /predict
│   ├── train_efficientnet_b0.py         # PyTorch transfer learning training script
│   ├── convert_to_tflite.py             # Model export to ONNX & TFLite
│   ├── class_mapping.json               # Index-to-label class mapping
│   ├── requirements.txt                 # Python dependencies
│   └── weights/                         # Directory for trained weights (.pt / .keras)
│       └── efficientnet_b0_tongue_cancer.pt
│
├── frontend-web/                        # React + TypeScript Web Application
│   ├── src/
│   │   ├── App.tsx                      # Web UI with preview, loading & prediction card
│   │   ├── App.css                      # Healthcare responsive design styles
│   │   ├── types.ts                     # TypeScript data interfaces
│   │   └── main.tsx
│   ├── index.html
│   ├── package.json
│   └── tsconfig.json
│
├── metadata.json                        # AI Studio Platform Metadata
└── README.md                            # Comprehensive Final Year Project Guide
```

---

## 4. Where to Configure Your Trained EfficientNet-B0 Model

To connect your real trained model weights:

1. **Place your trained weights file** in:
   ```text
   backend/weights/efficientnet_b0_tongue_cancer.pt
   ```
2. **Verify `backend/class_mapping.json`**:
   ```json
   {
     "0": "Benign / Normal Tongue Tissue",
     "1": "Squamous Cell Carcinoma (OSCC / Malignant)"
   }
   ```
3. **If weights are not found**:
   Both the Android app and Web frontend will display the exact required prompt message:
   > *"Prediction is unavailable because the trained EfficientNet-B0 model has not been configured."*

---

## 5. Dataset Preparation & Training Guidelines

### Critical Differentiation Rule: Tongue vs Oral Cavity Datasets
> **Important:** Do NOT assume that an oral cancer dataset is automatically a tongue cancer dataset.
> General oral cancer datasets frequently mix lesions from buccal mucosa, gingiva, floor of the mouth, hard palate, and retromolar trigone.
> Tongue Squamous Cell Carcinoma has distinct tissue morphology, papillary patterns, and lighting characteristics.

### Recommended Directory Structure:
```text
dataset/
├── train/
│   ├── 0_benign_normal/      # Normal dorsal papillae, benign ulcers, leukoplakia
│   └── 1_malignant_carcinoma/# Confirmed tongue OSCC biopsy cases
├── val/
│   ├── 0_benign_normal/
│   └── 1_malignant_carcinoma/
└── test/
    ├── 0_benign_normal/
    └── 1_malignant_carcinoma/
```

### Training Steps:
1. Navigate to the `backend` folder:
   ```bash
   cd backend
   pip install -r requirements.txt
   ```
2. Run the transfer learning training pipeline:
   ```bash
   python train_efficientnet_b0.py
   ```
3. This executes:
   - **Phase 1 (10 Epochs)**: Backbone frozen, trains the top classification head with Adam (`lr=1e-3`).
   - **Phase 2 (25 Epochs)**: Unfreezes top MBConv layers (stages 6 & 7) with cosine annealing learning rate (`lr=1e-4`).
   - Computes Loss, Accuracy, Sensitivity (Recall), Specificity, F1-Score, and ROC-AUC.
   - Saves best weights automatically to `weights/efficientnet_b0_tongue_cancer.pt`.

---

## 6. How to Run the Applications

### A. Run Python FastAPI Backend
```bash
cd backend
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```
- Interactive Swagger API Documentation: `http://localhost:8000/docs`
- Health check: `http://localhost:8000/health`
- Model info: `http://localhost:8000/model-info`

### B. Run React + TypeScript Web Frontend
```bash
cd frontend-web
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

### C. Run Android Application
1. In AI Studio, the application runs on the streaming emulator.
2. In Android Studio, open the project root and run on device or emulator.
3. In the Android App's **Model Setup** tab, point the backend URL to:
   - Android Emulator: `http://10.0.2.2:8000/` (maps to host localhost)
   - Physical Phone on Wi-Fi: `http://<YOUR_COMPUTER_IP>:8000/`

---

## 7. Medical Disclaimer & Ethics

> **Medical Disclaimer:**  
> *"This result is intended only for preliminary screening support. It does not confirm or rule out tongue cancer. Please consult a qualified healthcare professional for clinical examination and further testing."*

- Deep learning predictions must never be presented as definitive diagnoses.
- Ground truth must always be established through clinical biopsy and histopathological examination.
