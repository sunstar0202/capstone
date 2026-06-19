import io
import cv2
import numpy as np
from datetime import datetime
from typing import List

import torch
import torch.nn as nn
from PIL import Image
from torchvision import transforms, models

from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

class_names = ["CAN", "GLASS", "PET"]

model = models.mobilenet_v2(weights=None)
num_features = model.classifier[1].in_features
model.classifier[1] = nn.Linear(num_features, len(class_names))

MODEL_PATH = "trash_model.pth"

try:
    model.load_state_dict(torch.load(MODEL_PATH, map_location=device))
    model = model.to(device)
    model.eval()
    print("==================================================")
    print("✅ AI Trash Model loaded successfully.")
    print("==================================================")
except FileNotFoundError:
    print(f"❌ Error: {MODEL_PATH} not found in backend folder.")
except Exception as e:
    print(f"❌ Error loading AI model: {e}")

transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        [0.485, 0.456, 0.406],
        [0.229, 0.224, 0.225]
    )
])

class AnalysisResultRequest(BaseModel):
    label: str
    score: float
    deviceId: str = "tablet-01"

class AnalysisResultResponse(AnalysisResultRequest):
    id: int
    createdAt: datetime

results: List[AnalysisResultResponse] = []

def analyze_can_details(image_bytes: bytes, base_penalty: float) -> tuple:
    try:
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_GRAYSCALE)
        if img is None:
            return 0.0, 1, 1, 1

        img_resized = cv2.resize(img, (224, 224))
        blurred = cv2.GaussianBlur(img_resized, (5, 5), 0)
        edges = cv2.Canny(blurred, 50, 150)
        edge_pixels = np.sum(edges > 0)

        c1 = 3 if base_penalty > 12.0 else 1
        c2 = 2 if edge_pixels > 12000 else 1
        c3 = 3 if edge_pixels > 15000 else 1

        score = min(35.0, (edge_pixels / 500.0) * 10.0)
        return float(score), c1, c2, c3
    except:
        return 0.0, 1, 1, 1

def analyze_pet_details(image_bytes: bytes, base_penalty: float) -> tuple:
    try:
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_GRAYSCALE)
        if img is None:
            return 0.0, 1, 1, 1

        img_resized = cv2.resize(img, (224, 224))
        h, w = img_resized.shape
        bottom_zone = img_resized[int(h*0.75):h, :]
        mean_val, std_dev = cv2.meanStdDev(bottom_zone)

        c1 = 3 if base_penalty > 10.0 else 1
        c2 = 2 if std_dev[0][0] > 38.0 or mean_val[0][0] < 90 else 1
        c3 = 3 if mean_val[0][0] < 75 else 1

        score = 45.0 if c2 == 2 else 0.0
        return float(score), c1, c2, c3
    except:
        return 0.0, 1, 1, 1

def analyze_glass_details(image_bytes: bytes, base_penalty: float) -> tuple:
    try:
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_GRAYSCALE)
        if img is None:
            return 0.0, 1, 1, 1

        img_resized = cv2.resize(img, (224, 224))
        blurred = cv2.GaussianBlur(img_resized, (3, 3), 0)
        thresh = cv2.adaptiveThreshold(blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 11, 2)
        contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        defect_count = len(contours)

        c1 = 3 if defect_count > 25 else 1
        c2 = 2 if base_penalty > 12.0 else 1
        c3 = 3 if defect_count > 40 else 1

        score = min(35.0, defect_count * 1.5)
        return float(score), c1, c2, c3
    except:
        return 0.0, 1, 1, 1

@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "message": "Backend server is running"
    }

@app.post("/predict")
async def predict_trash(file: UploadFile = File(...)):
    try:
        image_bytes = await file.read()

        with open("check_pad_image.jpg", "wb") as f:
            f.write(image_bytes)

        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        input_tensor = transform(image).unsqueeze(0).to(device)

        with torch.no_grad():
            outputs = model(input_tensor)
            probabilities = torch.nn.functional.softmax(outputs, dim=1)
            confidence, preds = torch.max(probabilities, 1)

            label = class_names[preds.item()]
            base_penalty = float((1.0 - confidence.item()) * 25.0)
            total_score = base_penalty

            c1, c2, c3 = 1, 1, 1

            if label == "CAN":
                def_score, c1, c2, c3 = analyze_can_details(image_bytes, base_penalty)
                total_score += def_score
            elif label == "PET":
                liq_score, c1, c2, c3 = analyze_pet_details(image_bytes, base_penalty)
                total_score += liq_score
            elif label == "GLASS":
                glass_score, c1, c2, c3 = analyze_glass_details(image_bytes, base_penalty)
                total_score += glass_score

            pure_score = int(max(0.0, min(100.0, total_score)))
            encoded_score = float(pure_score + (c1 * 0.1) + (c2 * 0.01) + (c3 * 0.001))

        return {
            "id": len(results) + 1,
            "label": label,
            "score": encoded_score,
            "deviceId": "tablet-01",
            "createdAt": datetime.now().isoformat()
        }

    except Exception as e:
        print(f"🚨 Backend inner exception raised: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"AI analysis server error: {str(e)}"
        )