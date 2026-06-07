import io
from datetime import datetime
from typing import List

import torch
import torch.nn as nn
from PIL import Image
from torchvision import transforms, models

from fastapi import FastAPI, File, UploadFile, HTTPException
from pydantic import BaseModel

app = FastAPI()

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
    print("AI Trash Model loaded successfully.")
except FileNotFoundError:
    print(f"Error: {MODEL_PATH} not found in backend folder.")
except Exception as e:
    print(f"Error loading AI model: {e}")

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
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        input_tensor = transform(image).unsqueeze(0).to(device)

        with torch.no_grad():
            outputs = model(input_tensor)
            probabilities = torch.nn.functional.softmax(outputs, dim=1)
            confidence, preds = torch.max(probabilities, 1)

            label = class_names[preds.item()]
            score = float((1.0 - confidence.item()) * 30.0)

        return {
            "label": label,
            "score": round(score, 2)
        }

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"AI analysis server error: {str(e)}"
        )


@app.post("/analysis-result", response_model=AnalysisResultResponse)
def save_analysis_result(request: AnalysisResultRequest):
    result = AnalysisResultResponse(
        id=len(results) + 1,
        label=request.label,
        score=request.score,
        deviceId=request.deviceId,
        createdAt=datetime.now()
    )
    results.append(result)
    return result


@app.get("/analysis-results", response_model=List[AnalysisResultResponse])
def get_analysis_results():
    return results