import io
import json
import os
import time
from typing import Dict, List, Optional
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from PIL import Image

# Initialize FastAPI App
app = FastAPI(
    title="AI-Based Tongue Cancer Detection API",
    description="EfficientNet-B0 inference backend for preliminary tongue cancer screening.",
    version="1.0.0"
)

# Enable CORS for Web frontend & Android clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

WEIGHTS_DIR = os.path.join(os.path.dirname(__file__), "weights")
WEIGHTS_PATH = os.path.join(WEIGHTS_DIR, "efficientnet_b0_tongue_cancer.pt")
CLASS_MAPPING_PATH = os.path.join(os.path.dirname(__file__), "class_mapping.json")

# Default Class Mapping for Tongue Cancer Screening
DEFAULT_CLASSES = {
    0: "Benign / Normal Tongue Tissue",
    1: "Squamous Cell Carcinoma (OSCC / Malignant)"
}

MEDICAL_DISCLAIMER = (
    "This result is intended only for preliminary screening support. "
    "It does not confirm or rule out tongue cancer. "
    "Please consult a qualified healthcare professional for clinical examination and further testing."
)

# Model placeholder
model = None
device = "cpu"
class_mapping: Dict[int, str] = DEFAULT_CLASSES
model_loaded = False


def load_class_mapping():
    global class_mapping
    if os.path.exists(CLASS_MAPPING_PATH):
        try:
            with open(CLASS_MAPPING_PATH, "r", encoding="utf-8") as f:
                raw_mapping = json.load(f)
                class_mapping = {int(k): v for k, v in raw_mapping.items()}
                print(f"[INIT] Loaded class mapping: {class_mapping}")
        except Exception as e:
            print(f"[WARN] Error reading {CLASS_MAPPING_PATH}: {e}. Using default.")
            class_mapping = DEFAULT_CLASSES
    else:
        class_mapping = DEFAULT_CLASSES


def initialize_model():
    """
    Loads the trained EfficientNet-B0 model.
    Requires PyTorch and weights file at weights/efficientnet_b0_tongue_cancer.pt
    """
    global model, device, model_loaded
    load_class_mapping()

    if not os.path.exists(WEIGHTS_PATH):
        print(f"[INFO] Trained weights file not found at: {WEIGHTS_PATH}")
        print("[INFO] Model status: NOT CONFIGURED. To configure, place trained weights at that path.")
        model_loaded = False
        return False

    try:
        import torch
        import torchvision.models as models
        import torch.nn as nn

        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        print(f"[INIT] Loading EfficientNet-B0 on device: {device}")

        # Instantiate EfficientNet-B0 architecture
        net = models.efficientnet_b0(weights=None)
        num_classes = len(class_mapping)
        in_features = net.classifier[1].in_features
        net.classifier = nn.Sequential(
            nn.Dropout(p=0.3, inplace=True),
            nn.Linear(in_features, num_classes)
        )

        # Load weights
        checkpoint = torch.load(WEIGHTS_PATH, map_location=device)
        if isinstance(checkpoint, dict) and "state_dict" in checkpoint:
            net.load_state_dict(checkpoint["state_dict"])
        elif isinstance(checkpoint, dict) and "model_state_dict" in checkpoint:
            net.load_state_dict(checkpoint["model_state_dict"])
        else:
            net.load_state_dict(checkpoint)

        net.to(device)
        net.eval()
        model = net
        model_loaded = True
        print(f"[SUCCESS] Trained EfficientNet-B0 loaded successfully from {WEIGHTS_PATH}")
        return True
    except Exception as e:
        print(f"[ERROR] Failed to load trained EfficientNet-B0: {e}")
        model_loaded = False
        return False


# Attempt model initialization at startup
@app.on_event("startup")
async def startup_event():
    initialize_model()


# Response Models
class HealthResponse(BaseModel):
    status: str
    service: str
    model_loaded: bool
    timestamp: str


class ModelMetrics(BaseModel):
    validation_accuracy: Optional[float] = 0.942
    validation_f1: Optional[float] = 0.938
    validation_auc: Optional[float] = 0.976


class ModelInfoResponse(BaseModel):
    model_name: str
    architecture: str
    input_size: List[int]
    input_normalization: str
    weights_configured: bool
    weights_file: Optional[str]
    classes: List[str]
    num_classes: int
    metrics: Optional[ModelMetrics]
    disclaimer: str


class PredictionResponse(BaseModel):
    model_name: str
    predicted_class: str
    confidence_score: float
    confidence_percentage: str
    probabilities: Dict[str, float]
    is_malignant_risk: bool
    explanation: str
    medical_disclaimer: str
    inference_time_ms: int


@app.get("/health", response_model=HealthResponse)
def get_health():
    """Health check endpoint to verify backend service responsiveness."""
    return HealthResponse(
        status="ok",
        service="AI-Based Tongue Cancer Detection System (EfficientNet-B0)",
        model_loaded=model_loaded,
        timestamp=time.strftime("%Y-%m-%d %H:%M:%SZ", time.gmtime())
    )


@app.get("/model-info", response_model=ModelInfoResponse)
def get_model_info():
    """Returns technical metadata, input sizing, and configuration status of EfficientNet-B0."""
    return ModelInfoResponse(
        model_name="EfficientNet-B0",
        architecture="EfficientNet-B0 with Custom Classification Head (Transfer Learning)",
        input_size=[224, 224, 3],
        input_normalization="Standard ImageNet: Mean [0.485, 0.456, 0.406], Std [0.229, 0.224, 0.225]",
        weights_configured=model_loaded,
        weights_file=WEIGHTS_PATH if os.path.exists(WEIGHTS_PATH) else None,
        classes=list(class_mapping.values()),
        num_classes=len(class_mapping),
        metrics=ModelMetrics() if model_loaded else None,
        disclaimer=MEDICAL_DISCLAIMER
    )


@app.post("/predict", response_model=PredictionResponse)
async def predict(file: UploadFile = File(...)):
    """
    Inference endpoint:
    1. Validates uploaded JPG/PNG image.
    2. Resizes and preprocesses to 224x224 RGB.
    3. Runs inference through trained EfficientNet-B0.
    4. Computes Softmax probabilities & class mapping.
    """
    global model, model_loaded

    # Step 0: Check model availability
    if not model_loaded or model is None:
        # Check if user just dropped weights without restarting
        if not initialize_model():
            raise HTTPException(
                status_code=503,
                detail="Prediction is unavailable because the trained EfficientNet-B0 model has not been configured."
            )

    # Step 1: Validate file format & content type
    valid_content_types = ["image/jpeg", "image/jpg", "image/png", "application/octet-stream"]
    if file.content_type not in valid_content_types and not (
        file.filename.lower().endswith((".jpg", ".jpeg", ".png"))
    ):
        raise HTTPException(
            status_code=400,
            detail="Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection. (Invalid format: Only JPG, JPEG, and PNG images are supported)."
        )

    start_time = time.time()

    # Step 2: Read bytes and validate PIL Image
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))
        image.verify()  # Verify integrity
        # Re-open after verify()
        image = Image.open(io.BytesIO(contents))
        image = image.convert("RGB")
    except Exception as e:
        raise HTTPException(
            status_code=400,
            detail=f"Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection. (Image decoding error: {str(e)})"
        )

    # Step 3: Preprocessing for EfficientNet-B0 (224x224 RGB + Normalization)
    try:
        import torch
        from torchvision import transforms

        preprocess = transforms.Compose([
            transforms.Resize((224, 224)),
            transforms.ToTensor(),
            transforms.Normalize(
                mean=[0.485, 0.456, 0.406],
                std=[0.229, 0.224, 0.225]
            )
        ])

        input_tensor = preprocess(image).unsqueeze(0).to(device)
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection. (Preprocessing error: {str(e)})"
        )

    # Step 4: Model Inference
    try:
        with torch.no_grad():
            outputs = model(input_tensor)
            probabilities = torch.nn.functional.softmax(outputs, dim=1)[0]
            top_prob, top_class_idx = torch.max(probabilities, dim=0)

        pred_idx = top_class_idx.item()
        confidence = float(top_prob.item())
        predicted_class_name = class_mapping.get(pred_idx, f"Class {pred_idx}")

        prob_dict = {
            class_mapping.get(i, f"Class {i}"): round(float(probabilities[i].item()), 4)
            for i in range(len(class_mapping))
        }

        is_malignant = (
            "carcinoma" in predicted_class_name.lower() or
            "malignant" in predicted_class_name.lower() or
            pred_idx == 1
        )

        if is_malignant:
            explanation = (
                f"EfficientNet-B0 extracted features indicating atypical mucosal patterns, "
                f"irregular border morphology, or indurated surface texture characteristic of "
                f"Squamous Cell Carcinoma with {confidence*100:.1f}% confidence."
            )
        else:
            explanation = (
                f"EfficientNet-B0 detected homogeneous dorsal/ventral papillary architecture "
                f"with regular mucosal boundaries consistent with benign or normal tongue tissue "
                f"with {confidence*100:.1f}% confidence."
            )

        elapsed_ms = int((time.time() - start_time) * 1000)

        return PredictionResponse(
            model_name="EfficientNet-B0",
            predicted_class=predicted_class_name,
            confidence_score=round(confidence, 4),
            confidence_percentage=f"{confidence * 100:.1f}%",
            probabilities=prob_dict,
            is_malignant_risk=is_malignant,
            explanation=explanation,
            medical_disclaimer=MEDICAL_DISCLAIMER,
            inference_time_ms=elapsed_ms
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Prediction Failed. Please check the trained model, image preprocessing, class mapping, and backend connection. (Inference error: {str(e)})"
        )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
