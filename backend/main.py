from fastapi import FastAPI
from pydantic import BaseModel
from datetime import datetime
from typing import List

app = FastAPI()

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