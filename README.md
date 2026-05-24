# ♻️ AI 오염도 판별 스마트 분리수거 시스템
> **2026 캡스톤 디자인** | AI 객체 인식 기반 재활용 솔루션

---

## 📌 1. 프로젝트 개요
사용자가 분리수거함 앞에 섰을 때, 태블릿 카메라를 통해 품목을 인식하고 **내부 오염도(잔여물, 라벨 유무 등)**를 분석하여 
배출 가능 여부를 알려주는 스마트 분리수거 시스템

1. 안드로이드 스튜디오 설치
   버전: Android Studio Ladybug (2024.2.1) 이상 권장 (최신 안정화 버전)

설치 경로: 가급적 기본 경로 사용 (한글 폴더명이 포함된 경로는 에러 발생 확률 높음)

2. 핵심 버전 설정 (build.gradle.kts 기준)
   프로젝트 오픈 후 build.gradle.kts (Project)와 (Module :app)에서 아래 수치 확인

Compile SDK: 34 (Android 14)

Target SDK: 34

Min SDK: 26 (Android 8.0 - CameraX와 TFLite 안정 구동 하한선)

Kotlin Version: 1.9.0 이상

Java Version: JDK 17 (설정 -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK에서 확인)

3. 필수 라이브러리 (Dependencies)

CameraX: 실시간 영상 분석용

TensorFlow Lite: AI 모델 추론용 (api 및 support 포함)

Material Design: 카드뷰 및 버튼 UI용

## 📌 2. 주요 기능
🔵 Backend (Functionality)
AI 실시간 분석: CameraX와 TFLite를 연동하여 0.1초 내외의 빠른 객체 인식 및 오염도(%) 산출.

파일 관리 시스템: UUID 기반 파일명 저장으로 데이터 중복 방지 및 유형별(PDF, PPT, Audio) 자동 분류.

하드웨어 연동: 분석 통과 시 블루투스/시리얼 통신을 통해 쓰레기통 투껑(Servo Motor) 제어 신호 송신.

서버 상태 점검: /health 엔드포인트를 통한 실시간 서버 가용성 모니터링.

🟢 Frontend (User Experience)
태블릿 최적화 UI: 대화면을 활용한 7:3 비율의 카메라 프리뷰 및 결과창 배치.

오염도 상세 리포트: '라벨 미제거', '이물질 존재' 등 AI가 판단한 근거를 상세 팝업으로 제공.

사용자 시나리오 기반 흐름: 시작 -> 안내 -> 실시간 스캔 -> 결과 확인으로 이어지는 직관적 UX.

## 📌 2. 역할 분담
현준,우성: 라즈베리파이 혹은 아두이노 회로 담당(현준이는 코틀린 코드 중간중간 확인 부탁함)
선호,대연: 프론트엔드, 벡엔드, 전체적인 앱 등 코드 담당

현준이랑 우성이는 스마트 분리수거 시스템에 맞게 앱에서 인식하였을 때 쓰레기통 뚜껑이 알맞게 열릴 수 있게끔
회로 구성 잘 만들어주면 될거 같고

선호랑 대연이는 선호는 프론트엔드 담당해서 만들고, 대연이는 벡엔드 담당을 해주면 될듯함 
현재 진행상황: 프론트엔드는 거의 다 만듬
깃허브에 올릴거니까 확인해서 벡엔드만 만들어주면 될듯함.

#######################################################
`backend/` 폴더를 새로 만들고 FastAPI로 서버 추가했습니다

추가된 주요 파일:

backend/main.py
backend/requirements.txt

현재 백엔드 API:

GET /health
서버 상태 확인용
POST /analysis-result
앱에서 분석 결과를 서버로 전송
GET /analysis-results
서버에 저장된 분석 결과 목록 조회

앱이 서버랑 연결할 수 있어요
```text
카메라 촬영
→ AI 분석
→ 결과(label, score)
→ 서버로 전송
```

3. 실시간 분석은 아직 안됩니다.

처음에는 카메라 키면 바로 분석되도록 했는데,
컴퓨터가 멈출 정도로 부하가 커서 나중으로 미룹니다.

```text
분석 버튼 눌렀을 때만
1회 분석 + 서버 전송
```

4. 현재 테스트는 가능한 상태입니다.

지금 가능한 것:

* 카메라 프리뷰
* AI 분석
* 서버 전송
* 블루투스 신호 전송

5. 서버 실행 방법은 다들 알겠지만 일단 올립니다.

프로젝트 pull 받은 뒤

```powershell
cd backend
python -m venv venv
.\venv\Scripts\activate
pip install -r requirements.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8080
```

서버 확인

```text
http://127.0.0.1:8080/health
```

API 문서 확인 

```text
http://127.0.0.1:8080/docs
```

6. 주의사항

* `backend/venv/`는 GitHub에 올리면 안 됩니다.
* 서버 실행 안 하면 앱에서 서버 전송 실패합니다.
* 현재 DB는 없고 메모리에만 저장됩니다. (서버 끄면 데이터 사라짐)
* 블루투스 MAC 주소는 아직 실제 값 입력 필요합니다.
* 제발 리드미 꼭 좀 읽고 아래 프로젝트 구조 바꿔놨으니깐 참고 바랍니다.
* 아니면 물어보던가

7. 다음 작업 예정

* 기존 UI 다시 복구
* 상세 결과 화면 연결
* DB 연동
* 실제 기기 테스트
* 아두이노 연동 테스트
#######################################################

## 📂 3. 프로젝트 구조 (Project Structure)
```text
Capstone1/
├── app/                              # Android 앱 모듈
│   └── src/
│       └── main/
│           ├── java/com/example/capstone/
│           │   ├── MainActivity.kt          # 카메라, AI 분석, 서버 전송, 블루투스 제어
│           │   ├── StartActivity.kt         # 시작 화면
│           │   ├── TrashClassifier.kt       # TFLite 기반 AI 추론 로직
│           │   ├── BluetoothManager.kt      # 아두이노 블루투스 제어 로직
│           │   ├── AnalysisRequest.kt       # 서버 전송용 데이터 모델
│           │   ├── ApiService.kt            # Retrofit API 인터페이스
│           │   └── RetrofitClient.kt        # Retrofit 클라이언트 설정
│           │
│           ├── res/layout/                  # Android XML UI
│           │   ├── activity_start.xml       # 시작 화면
│           │   └── activity_main.xml        # 카메라 프리뷰 및 분석 화면
│           │
│           ├── assets/
│           │   └── model.tflite             # 학습된 TFLite 모델 파일
│           │
│           └── AndroidManifest.xml          # 앱 권한 및 Activity 설정
│
├── backend/                         # FastAPI 백엔드 서버
│   ├── main.py                      # API 엔드포인트
│   ├── requirements.txt             # Python 패키지 목록
│   └── venv/                        # 로컬 가상환경 (GitHub 업로드 제외)
│
├── build.gradle.kts                 # 프로젝트 Gradle 설정
├── settings.gradle.kts              # Gradle 모듈 설정
└── README.md

```


## 4 라즈베리파이 구조

전체 구조:

```text
Android 앱
 → BluetoothManager.send("O")
 → Raspberry Pi 4 내장 Bluetooth 수신
 → GPIO18 PWM 출력
 → SG90 서보모터 회전
 → 쓰레기통 뚜껑 열림
```
```text
우리 키트 블루투스 주소
DC:A6:32:88:E1:CB
```

# 최종 하드웨어 구성

```text
┌─────────────────────┐
│ Android App         │
│ TrashClassifier     │
│ BluetoothManager    │
└─────────┬───────────┘
          │ Bluetooth
          ▼
┌─────────────────────┐
│ Raspberry Pi 4      │
│ Python Bluetooth    │
│ GPIO18 PWM          │
└─────────┬───────────┘
          │ PWM Signal
          ▼
┌─────────────────────┐
│ SG90 Servo Motor    │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│ Trash Bin Lid       │
└─────────────────────┘
```

---
# 1. 준비물

* Raspberry Pi 4 Model B
* SG90 서보모터
* 점퍼선
* 5V 2A 어댑터(권장)
* 쓰레기통

---
# 2. Raspberry Pi 4 GPIO 전선 연결

## SG90 서보모터 선 색상

| 서보 선 | 역할      |
| ---- | ------- |
| 빨강   | 전원(VCC) |
| 갈색   | GND     |
| 주황   | 신호(PWM) |

## Raspberry Pi 4 연결

| 서보모터       | Raspberry Pi 4 GPIO |
| ---------- | ------------------- |
| 빨강(VCC)    | 5V (핀 2 또는 4)       |
| 갈색(GND)    | GND (핀 6)           |
| 주황(Signal) | GPIO18 (핀 12)       |

---
# 3. GPIO 핀 도안

```text
Raspberry Pi 4 GPIO
  3.3V (1) (2) 5V  ← 서보 빨강
 GPIO2 (3) (4) 5V
 GPIO3 (5) (6) GND ← 서보 갈색
 GPIO4 (7) (8) GPIO14
   GND (9) (10) GPIO15
GPIO17(11) (12) GPIO18 ← 서보 주황
```

---
# 4. 실제 연결 도안

```text
        ┌─────────────────────┐
        │ Raspberry Pi 4      │
        │                     │
        │ GPIO18 (핀12) ─────────────┐
        │ GND    (핀6)  ───────┐     │
        │ 5V     (핀2)  ────┐  │     │
        └─────────────────│──│─────│──┘
                          │  │     │
                          │  │     │
                     ┌────▼──▼─────▼───┐
                     │ SG90 Servo Motor │
                     │                  │
                     │ 빨강  → VCC      │
                     │ 갈색  → GND      │
                     │ 주황  → SIGNAL   │
                     └──────────────────┘
```

---









