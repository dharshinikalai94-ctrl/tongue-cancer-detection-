"""
AI-Based Tongue Cancer Detection System Using EfficientNet-B0
Transfer Learning & Model Training Pipeline

Project Objective:
Train an EfficientNet-B0 deep convolutional neural network for tongue cancer screening.
"""

import os
import json
import time
import copy
from typing import Tuple

import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
from torchvision import datasets, models, transforms
from sklearn.metrics import classification_report, confusion_matrix, roc_auc_score
import numpy as np

# 1. Configuration & Hyperparameters
CONFIG = {
    "data_dir": "dataset",           # Root folder: dataset/train, dataset/val, dataset/test
    "output_dir": "weights",
    "weights_filename": "efficientnet_b0_tongue_cancer.pt",
    "class_mapping_filename": "class_mapping.json",
    "image_size": 224,               # EfficientNet-B0 native resolution
    "batch_size": 32,
    "num_workers": 4,
    "epochs_phase1": 10,             # Feature extractor head training
    "epochs_phase2": 25,             # Fine-tuning unfrozen top MBConv layers
    "lr_phase1": 1e-3,
    "lr_phase2": 1e-4,
    "weight_decay": 1e-4,
    "dropout_rate": 0.3,
    "seed": 42
}

def set_seed(seed=42):
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    np.random.seed(seed)

def get_data_transforms(img_size: int = 224):
    """
    Data Augmentation & Preprocessing Pipeline
    - Training: Rotation (+-15 deg), Flips, Color Jitter (lighting variation)
    - Validation/Testing: Clean resize & ImageNet normalization
    """
    imagenet_mean = [0.485, 0.456, 0.406]
    imagenet_std = [0.229, 0.224, 0.225]

    train_transforms = transforms.Compose([
        transforms.Resize((img_size, img_size)),
        transforms.RandomHorizontalFlip(p=0.5),
        transforms.RandomVerticalFlip(p=0.2),
        transforms.RandomRotation(degrees=15),
        transforms.ColorJitter(brightness=0.1, contrast=0.1, saturation=0.1),
        transforms.ToTensor(),
        transforms.Normalize(mean=imagenet_mean, std=imagenet_std)
    ])

    eval_transforms = transforms.Compose([
        transforms.Resize((img_size, img_size)),
        transforms.ToTensor(),
        transforms.Normalize(mean=imagenet_mean, std=imagenet_std)
    ])

    return train_transforms, eval_transforms

def prepare_dataloaders(data_dir: str, batch_size: int, img_size: int):
    train_tf, eval_tf = get_data_transforms(img_size)

    train_dir = os.path.join(data_dir, "train")
    val_dir = os.path.join(data_dir, "val")
    test_dir = os.path.join(data_dir, "test")

    if not os.path.exists(train_dir):
        raise FileNotFoundError(
            f"Dataset train directory not found at '{train_dir}'. "
            f"Please ensure dataset is structured with 'train', 'val', and 'test' folders."
        )

    train_dataset = datasets.ImageFolder(train_dir, transform=train_tf)
    val_dataset = datasets.ImageFolder(val_dir, transform=eval_tf) if os.path.exists(val_dir) else None
    test_dataset = datasets.ImageFolder(test_dir, transform=eval_tf) if os.path.exists(test_dir) else None

    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True, num_workers=2, pin_memory=True)
    val_loader = DataLoader(val_dataset, batch_size=batch_size, shuffle=False, num_workers=2) if val_dataset else None
    test_loader = DataLoader(test_dataset, batch_size=batch_size, shuffle=False, num_workers=2) if test_dataset else None

    class_names = train_dataset.classes
    print(f"[DATASET] Classes detected: {class_names}")
    print(f"[DATASET] Train size: {len(train_dataset)} images")
    if val_dataset:
        print(f"[DATASET] Val size: {len(val_dataset)} images")
    if test_dataset:
        print(f"[DATASET] Test size: {len(test_dataset)} images")

    return train_loader, val_loader, test_loader, class_names

def build_efficientnet_b0(num_classes: int, dropout: float = 0.3):
    """
    Builds EfficientNet-B0 with ImageNet pretrained backbone and custom classification head.
    """
    weights = models.EfficientNet_B0_Weights.DEFAULT
    model = models.efficientnet_b0(weights=weights)

    # Freeze backbone initially
    for param in model.features.parameters():
        param.requires_grad = False

    # Replace classifier
    in_features = model.classifier[1].in_features
    model.classifier = nn.Sequential(
        nn.Dropout(p=dropout, inplace=True),
        nn.Linear(in_features, num_classes)
    )

    return model

def train_one_epoch(model, dataloader, criterion, optimizer, device):
    model.train()
    running_loss = 0.0
    correct = 0
    total = 0

    for inputs, labels in dataloader:
        inputs = inputs.to(device)
        labels = labels.to(device)

        optimizer.zero_grad()
        outputs = model(inputs)
        loss = criterion(outputs, labels)
        loss.backward()
        optimizer.step()

        running_loss += loss.item() * inputs.size(0)
        _, preds = torch.max(outputs, 1)
        correct += torch.sum(preds == labels.data).item()
        total += labels.size(0)

    epoch_loss = running_loss / total
    epoch_acc = correct / total
    return epoch_loss, epoch_acc

def evaluate(model, dataloader, criterion, device):
    model.eval()
    running_loss = 0.0
    correct = 0
    total = 0

    all_preds = []
    all_labels = []
    all_probs = []

    with torch.no_grad():
        for inputs, labels in dataloader:
            inputs = inputs.to(device)
            labels = labels.to(device)

            outputs = model(inputs)
            loss = criterion(outputs, labels)

            running_loss += loss.item() * inputs.size(0)
            probs = torch.softmax(outputs, dim=1)
            _, preds = torch.max(outputs, 1)

            correct += torch.sum(preds == labels.data).item()
            total += labels.size(0)

            all_preds.extend(preds.cpu().numpy())
            all_labels.extend(labels.cpu().numpy())
            all_probs.extend(probs.cpu().numpy())

    epoch_loss = running_loss / total
    epoch_acc = correct / total
    return epoch_loss, epoch_acc, np.array(all_labels), np.array(all_preds), np.array(all_probs)

def main():
    set_seed(CONFIG["seed"])
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"[DEVICE] Training on: {device}")

    # Check dataset
    if not os.path.exists(CONFIG["data_dir"]):
        print(f"[ERROR] Dataset directory '{CONFIG['data_dir']}' not found.")
        print("Please create the dataset folder with subdirectories: train/0_benign, train/1_malignant, etc.")
        return

    train_loader, val_loader, test_loader, class_names = prepare_dataloaders(
        CONFIG["data_dir"], CONFIG["batch_size"], CONFIG["image_size"]
    )

    num_classes = len(class_names)
    model = build_efficientnet_b0(num_classes, CONFIG["dropout_rate"])
    model.to(device)

    criterion = nn.CrossEntropyLoss()

    # PHASE 1: Train Head Only
    print("\n--- PHASE 1: Training Classification Head (Backbone Frozen) ---")
    optimizer = optim.Adam(model.classifier.parameters(), lr=CONFIG["lr_phase1"], weight_decay=CONFIG["weight_decay"])

    best_val_acc = 0.0
    best_weights = copy.deepcopy(model.state_dict())

    for epoch in range(CONFIG["epochs_phase1"]):
        t_loss, t_acc = train_one_epoch(model, train_loader, criterion, optimizer, device)
        v_loss, v_acc = (0.0, 0.0)
        if val_loader:
            v_loss, v_acc, _, _, _ = evaluate(model, val_loader, criterion, device)
            print(f"Epoch {epoch+1:02d}/{CONFIG['epochs_phase1']} - Train Loss: {t_loss:.4f} Acc: {t_acc:.4f} | Val Loss: {v_loss:.4f} Acc: {v_acc:.4f}")
            if v_acc > best_val_acc:
                best_val_acc = v_acc
                best_weights = copy.deepcopy(model.state_dict())
        else:
            print(f"Epoch {epoch+1:02d}/{CONFIG['epochs_phase1']} - Train Loss: {t_loss:.4f} Acc: {t_acc:.4f}")

    # PHASE 2: Fine-Tuning Unfrozen Top MBConv Blocks (Stages 6 & 7)
    print("\n--- PHASE 2: Fine-Tuning Top Convolutional Layers ---")
    for param in model.features[6:].parameters():
        param.requires_grad = True

    optimizer_ft = optim.AdamW([
        {"params": model.features[6:].parameters(), "lr": CONFIG["lr_phase2"] * 0.1},
        {"params": model.classifier.parameters(), "lr": CONFIG["lr_phase2"]}
    ], weight_decay=CONFIG["weight_decay"])

    scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer_ft, T_max=CONFIG["epochs_phase2"])

    for epoch in range(CONFIG["epochs_phase2"]):
        t_loss, t_acc = train_one_epoch(model, train_loader, criterion, optimizer_ft, device)
        scheduler.step()

        if val_loader:
            v_loss, v_acc, _, _, _ = evaluate(model, val_loader, criterion, device)
            print(f"FineTune {epoch+1:02d}/{CONFIG['epochs_phase2']} - Train Loss: {t_loss:.4f} Acc: {t_acc:.4f} | Val Loss: {v_loss:.4f} Acc: {v_acc:.4f}")
            if v_acc > best_val_acc:
                best_val_acc = v_acc
                best_weights = copy.deepcopy(model.state_dict())
        else:
            print(f"FineTune {epoch+1:02d}/{CONFIG['epochs_phase2']} - Train Loss: {t_loss:.4f} Acc: {t_acc:.4f}")

    # Load best weights
    model.load_state_dict(best_weights)

    # Final Evaluation on Independent Test Set
    if test_loader:
        print("\n--- FINAL TEST EVALUATION ---")
        _, test_acc, y_true, y_pred, y_probs = evaluate(model, test_loader, criterion, device)
        print(f"Test Accuracy: {test_acc * 100:.2f}%")
        print("\nClassification Report:")
        print(classification_report(y_true, y_pred, target_names=class_names))
        print("Confusion Matrix:")
        print(confusion_matrix(y_true, y_pred))

        if num_classes == 2:
            auc = roc_auc_score(y_true, y_probs[:, 1])
            print(f"ROC-AUC: {auc:.4f}")

    # Save Trained Weights and Class Mapping
    os.makedirs(CONFIG["output_dir"], exist_ok=True)
    out_weights_path = os.path.join(CONFIG["output_dir"], CONFIG["weights_filename"])
    torch.save(model.state_dict(), out_weights_path)
    print(f"\n[SAVE] Model weights saved to: {out_weights_path}")

    class_mapping = {i: name for i, name in enumerate(class_names)}
    with open(CONFIG["class_mapping_filename"], "w", encoding="utf-8") as f:
        json.dump(class_mapping, f, indent=2)
    print(f"[SAVE] Class mapping saved to: {CONFIG['class_mapping_filename']}")

if __name__ == "__main__":
    main()
