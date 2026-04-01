# ♻️ CoreNote: AI 오염도 판별 스마트 분리수거 시스템
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

## 📂 3. 프로젝트 구조 (Project Structure)
```text
Capstone1/
├── app/
│   ├── main.py              # FastAPI 백엔드 (API 엔드포인트)
│   ├── TrashClassifier.kt   # TFLite 기반 AI 추론 로직
│   └── BluetoothManager.kt  # 아두이노(쓰레기통) 제어 로직
├── res/layout/              # 프론트엔드 XML (Android UI)
│   ├── activity_start.xml   # 대기 및 시작 화면
│   ├── activity_main.xml    # 실시간 스캔 및 분석 화면
│   ├── dialog_detail.xml    # 오염도 상세 분석 리포트 (팝업)
│   └── activity_result.xml  # 분리수거 완료 및 감사 화면
├── assets/
│   └── model.tflite         # 학습된 딥러닝 모델 파일
└── uploads/
