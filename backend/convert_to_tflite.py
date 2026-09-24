"""
Convert Trained EfficientNet-B0 to ONNX and TensorFlow Lite (TFLite)
For on-device edge deployment in mobile and embedded clinical screening systems.
"""

import os
import torch
import torchvision.models as models
import torch.nn as nn

WEIGHTS_PATH = "weights/efficientnet_b0_tongue_cancer.pt"
ONNX_PATH = "weights/efficientnet_b0_tongue_cancer.onnx"
NUM_CLASSES = 2

def convert_to_onnx():
    if not os.path.exists(WEIGHTS_PATH):
        print(f"[ERROR] Trained weights not found at {WEIGHTS_PATH}. Please train the model first.")
        return

    print("[1/2] Loading PyTorch model...")
    model = models.efficientnet_b0(weights=None)
    in_features = model.classifier[1].in_features
    model.classifier = nn.Sequential(
        nn.Dropout(p=0.3, inplace=True),
        nn.Linear(in_features, NUM_CLASSES)
    )

    state_dict = torch.load(WEIGHTS_PATH, map_location="cpu")
    model.load_state_dict(state_dict)
    model.eval()

    dummy_input = torch.randn(1, 3, 224, 224)
    print(f"[2/2] Exporting to ONNX: {ONNX_PATH}")
    torch.onnx.export(
        model,
        dummy_input,
        ONNX_PATH,
        export_params=True,
        opset_version=14,
        do_constant_folding=True,
        input_names=["input"],
        output_names=["output"],
        dynamic_axes={"input": {0: "batch_size"}, "output": {0: "batch_size"}}
    )
    print(f"[SUCCESS] ONNX model exported to {ONNX_PATH}")
    print("[INFO] To convert ONNX to TFLite:")
    print("   onnx2tf -i weights/efficientnet_b0_tongue_cancer.onnx -o weights/saved_model_tf")
    print("   tflite_convert --saved_model_dir=weights/saved_model_tf --output_file=weights/model.tflite")

if __name__ == "__main__":
    convert_to_onnx()
