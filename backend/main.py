import io
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


            score = float((1.0 - confidence.item()) * 30.0)


            prob_list = [round(p * 100, 2) for p in probabilities.tolist()[0]]
            print(f"\n[AI 실시간 연산 분석 정보]")
            print(f"▶️ 전체 확률 분포 (CAN, GLASS, PET 순서): {prob_list}%")
            print(f"▶️ 선택된 인덱스 번호: {preds.item()} | 매칭된 라벨: {label}")
            print(f"▶️ 확신도(Confidence): {round(confidence.item() * 100, 2)}% | 계산된 오염도: {round(score, 2)}%\n")

        return {
            "label": label,
            "score": round(score, 2)
        }

    except Exception as e:
        print(f"🚨 서버 내부에러 발생: {str(e)}")
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