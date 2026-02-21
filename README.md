# Code Repository Ingestion Service

<br/>
<img width="701" height="313" alt="image" src="https://github.com/user-attachments/assets/19fdc693-5d5b-4105-b67d-53f7a79a69b1" />
<br/>
## 📋 프로젝트 소개

<img width="728" height="210" alt="image" src="https://github.com/user-attachments/assets/f4b80ac7-8075-4d02-b2d1-3966b02cdce2" />
<img width="828" height="896" alt="image" src="https://github.com/user-attachments/assets/51ff0771-3bb3-447f-9f2f-0aed60a655dc" />
<br/>

### 개요

**Code Repository Ingestion Service**는 AI 에이전트가 직접 사용할 수 있는 엔터프라이즈급 코드 저장소 수집 서비스입니다. <br/>
GitLab, GitHub 등 다양한 소스 저장소에서 코드를 자동으로 수집하고, 변경 사항을 추적하며, 임베딩 서버로 전송하는 완전 자동화된 파이프라인을 제공합니다.<br/>

### 핵심 가치

- **AI 에이전트 중심 설계**: 인간이 아닌 AI 에이전트가 API를 통해 직접 사용
- **대규모 처리**: 수백~수천 개의 프로젝트를 효율적으로 병렬 처리
- **증분 업데이트**: 변경된 파일만 선별적으로 처리하여 리소스 절약
- **자동 복구**: 실패 시 지수 백오프 전략으로 자동 재시도
- **엔터프라이즈 아키텍처**: DDD 기반 클린 아키텍처로 유지보수성 극대화

### 주요 기능

1. **이중 수집 모드**
   - **FULL 모드**: 최초 수집 시 모든 프로젝트를 완전히 처리
   - **INCREMENTAL 모드**: 변경된 프로젝트만 선별적으로 처리

2. **지능형 변경 감지**
   - GitLab API 기반 프로젝트 레벨 변경 감지
   - JGit diff 기반 파일 레벨 변경 감지
   - 이중 검증으로 정확도 향상

3. **고급 파일 필터링**
   - 바이너리 파일 자동 감지
   - 크기 제한 및 패턴 기반 필터링
   - 스마트 기본값 (`.git`, `node_modules` 등 자동 제외)

4. **비동기 작업 처리**
   - REST API 기반 작업 생성 및 추적
   - 작업 큐를 통한 비동기 처리
   - 실시간 진행 상황 모니터링

5. **자동 재시도 메커니즘**
   - 지수 백오프 전략
   - 재시도 가능 에러 자동 분류
   - 부분 실패 처리

### 기술 스택

- **Backend**: Kotlin 2.2+, Spring Boot 4.x, Java 21, Kotlin Coroutines
- **데이터베이스**: MongoDB (Spring Data MongoDB)
- **Git**: JGit 6.9+, GitLab API (GitLab4J 6.2+), HTTP 클라이언트 OkHttp 4.12+
- **ingestion-ui**: Next.js 16, React 19, TypeScript
- **embedding-server**: Python 3.12+, FastAPI, Chroma, sentence-transformers

---

## 📁 프로젝트 구조

```
CodeSyncPro/
├── backend/           # 수집 API 서버 (Kotlin, Spring Boot 4)
├── ingestion-ui/       # 작업 목록·상세·생성 UI (Next.js 16)
├── embedding-server/   # 임베딩·벡터 저장·RAG 검색 (FastAPI)
└── README.md
```

- **backend**: 작업 CRUD, 큐 처리, GitLab 연동, 임베딩 서버로 전송
- **ingestion-ui**: 백엔드 API 호출, 작업 목록/상세/생성 화면
- **embedding-server**: 백엔드가 보낸 문서 임베딩 저장 및 검색 API

---

## 🚀 시작하기 (실행 방법)

### 사전 요구 사항

- **Java 21**, **Node.js 18+** (pnpm 권장), **Python 3.12+**
- **MongoDB** 실행 중 (로컬 기본: `localhost:27017`)

### MongoDB

- 로컬에서 인증 없이 사용: `MONGODB_URI=mongodb://localhost:27017/ingestion_service` 로 실행
- 인증 사용 시: `application.yml`의 `spring.data.mongodb.uri` 또는 환경 변수 `MONGODB_URI`에 `mongodb://사용자:비밀번호@localhost:27017/ingestion_service?authSource=admin` 형식으로 설정
- URI가 비어 있거나 인증 정보가 없으면 백엔드 `MongoConfig`가 기본 인증 URI를 사용합니다.

---

## 🏗️ 프로젝트 아키텍처

### 아키텍처 개요

본 프로젝트는 **Domain-Driven Design (DDD)** 기반의 **Clean Architecture**를 따릅니다. 계층별로 명확한 책임을 분리하여 테스트 용이성, 유지보수성, 확장성을 확보했습니다.

### 계층 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  REST API Controllers                                   │ │
│  │  - JobController                                        │ │
│  │  - DTOs & Mappers                                       │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                          │
│  ┌──────────────────┐  ┌─────────────────────────────────┐ │
│  │  Use Cases       │  │  Workers                        │ │
│  │  - CreateJob     │  │  - JobProcessor                 │ │
│  │  - GetJobStatus  │  │                                 │ │
│  │  - ProcessJob    │  │                                 │ │
│  │  - RetryItems   │  │                                 │ │
│  │  - CancelJob     │  │                                 │ │
│  └──────────────────┘  └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Models     │  │    Ports     │  │   Services   │      │
│  │              │  │  (Interfaces)│  │              │      │
│  │  - Job       │  │  - Repository │  │  - Retry     │      │
│  │  - JobItem   │  │  - Queue     │  │    Policy    │      │
│  │  - Project   │  │  - Catalog   │  │              │      │
│  │  - State     │  │  - Scanner   │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                Infrastructure Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Persistence │  │  External    │  │  FileSystem  │      │
│  │              │  │  Services    │  │              │      │
│  │  - MongoDB   │  │  - GitLab    │  │  - Scanner   │      │
│  │  - Queue     │  │  - Embedding │  │  - Filter    │      │
│  │              │  │  - JGit      │  │  - Binary     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 계층별 상세 설명

#### 1. Presentation Layer (표현 계층)

**책임**: 외부 인터페이스 제공 및 요청/응답 변환

- **JobController**: REST API 엔드포인트 제공
  - `GET /api/v1/jobs`: 작업 목록 조회 (쿼리: `limit`, `offset`)
  - `POST /api/v1/jobs`: 작업 생성
  - `GET /api/v1/jobs/{jobId}`: 작업 상태 조회
  - `POST /api/v1/jobs/{jobId}/retry`: 실패 항목 재시도
  - `DELETE /api/v1/jobs/{jobId}`: 작업 취소

- **DTOs**: 요청/응답 데이터 전송 객체
  - `CreateJobRequest`: 작업 생성 요청
  - `JobStatusResponse`: 작업 상태 응답
  - `RetryRequest`: 재시도 요청

#### 2. Application Layer (애플리케이션 계층)

**책임**: 비즈니스 유스케이스 구현 및 워크플로우 조율

- **Use Cases**:
  - `CreateJobUseCase`: 작업 생성 및 큐에 등록
  - `GetJobStatusUseCase`: 작업 상태 조회
  - `ProcessJobUseCase`: 실제 작업 처리 (핵심 로직)
  - `RetryFailedItemsUseCase`: 실패 항목 재시도
  - `CancelJobUseCase`: 작업 취소

- **Workers**:
  - `JobProcessor`: 백그라운드 작업 처리 워커. 기동 시 DB의 PENDING 작업을 큐에 재등록하여 재시작 후에도 미처리 작업이 처리되도록 함.

#### 3. Domain Layer (도메인 계층)

**책임**: 핵심 비즈니스 로직 및 도메인 모델

- **Models (엔티티/값 객체)**:
  - `Job`: 작업 집계 루트 (Aggregate Root)
  - `JobItem`: 작업 항목 엔티티
  - `Project`: 프로젝트 정보
  - `ProjectState`: 프로젝트 상태 추적

- **Ports (인터페이스)**:
  - `JobRepository`: 작업 저장소 인터페이스
  - `JobQueue`: 작업 큐 인터페이스
  - `ProjectCatalog`: 프로젝트 카탈로그 인터페이스
  - `ProjectStateStore`: 프로젝트 상태 저장소 인터페이스
  - `RepositorySynchronizer`: 저장소 동기화 인터페이스
  - `FileScanner`: 파일 스캐너 인터페이스
  - `EmbeddingClient`: 임베딩 API 클라이언트 인터페이스

- **Services (도메인 서비스)**:
  - `RetryPolicy`: 재시도 정책 도메인 서비스

#### 4. Infrastructure Layer (인프라스트럭처 계층)

**책임**: 외부 시스템 연동 및 기술적 구현

- **Persistence**:
  - `MongoJobRepository`: MongoDB 작업 저장소 구현
  - `MongoProjectStateStore`: MongoDB 프로젝트 상태 저장소 구현
  - `InMemoryJobQueue`: 인메모리 작업 큐 (프로덕션에서는 Redis/RabbitMQ 권장)

- **External Services**:
  - `GitlabProjectCatalog`: GitLab API 클라이언트 구현
  - `JGitRepositorySynchronizer`: JGit 기반 저장소 동기화
  - `HttpEmbeddingClient`: HTTP 기반 임베딩 API 클라이언트

- **FileSystem**:
  - `DefaultFileScanner`: 파일 스캐너 구현
  - `PathFilter`: 경로 필터링
  - `BinaryDetector`: 바이너리 파일 감지

### 설계 원칙

1. **의존성 역전 원칙 (DIP)**
   - 상위 계층이 하위 계층의 인터페이스에 의존
   - 구현체는 Infrastructure 계층에 위치

2. **단일 책임 원칙 (SRP)**
   - 각 클래스는 하나의 책임만 가짐
   - Use Case는 하나의 비즈니스 시나리오만 처리

3. **개방-폐쇄 원칙 (OCP)**
   - Port 인터페이스를 통해 확장 가능
   - 새로운 구현체 추가 시 기존 코드 수정 불필요

4. **도메인 중심 설계**
   - 비즈니스 로직은 Domain 계층에 집중
   - 기술적 세부사항은 Infrastructure 계층에 격리

### 데이터 흐름

```
1. API 요청
   ↓
2. Controller (DTO 변환)
   ↓
3. Use Case (비즈니스 로직 실행)
   ↓
4. Domain Service (도메인 규칙 적용)
   ↓
5. Repository (도메인 모델 저장/조회)
   ↓
6. Infrastructure (MongoDB/외부 API 호출)
   ↓
7. 응답 반환
```

### 비동기 처리 흐름

```
1. 작업 생성 (CreateJobUseCase)
   ↓
2. JobQueue에 등록
   ↓
3. JobProcessor가 큐에서 작업 가져오기
   ↓
4. ProcessJobUseCase 실행
   ↓
5. 프로젝트 목록 조회 (GitLab API)
   ↓
6. 프로젝트별 병렬 처리
   ↓
7. 파일 스캔 및 임베딩 전송
   ↓
8. 상태 업데이트 및 저장
```

---

## 📊 ERD (Entity Relationship Diagram)

### 데이터베이스 스키마

본 프로젝트는 **MongoDB**를 사용하며, 두 개의 주요 컬렉션을 관리합니다.

### 컬렉션 구조

#### 1. jobs 컬렉션 (작업 정보)

```json
{
  "_id": "job-abc123def456",
  "sourceType": "GITLAB",
  "sourceConfig": {
    "baseUrl": "https://gitlab.example.com",
    "accessToken": "glpat-xxx",
    "projectIds": [123, 456],
    "groupIds": [],
    "targetBranch": "main",
    "shouldIncludeSubgroups": true,
    "shouldIncludeArchived": false,
    "shouldUseMembershipOnly": true,
    "pageSize": 100
  },
  "options": {
    "mode": "FULL",
    "fileFilters": {
      "includeGlobs": ["**/*.kt"],
      "excludeDirs": [".git"],
      "excludeFiles": [],
      "maxFileSizeBytes": 5000000,
      "skipBinary": true
    },
    "concurrency": {
      "projects": 2,
      "files": 8
    },
    "cleanupAfterIngest": true,
    "since": null
  },
  "status": "PROCESSING",
  "progress": {
    "totalProjects": 150,
    "processedProjects": 45,
    "totalFiles": 12500,
    "processedFiles": 3200,
    "failedFiles": 3,
    "skippedFiles": 0
  },
  "createdAt": "2024-01-15T10:00:00Z",
  "startedAt": "2024-01-15T10:00:05Z",
  "completedAt": null,
  "cancelledAt": null,
  "callbackUrl": "https://webhook.example.com/job-complete",
  "items": [
    {
      "id": "item-001",
      "projectPath": "group/project1",
      "filePath": "src/Main.kt",
      "status": "SUCCESS",
      "error": null,
      "retryCount": 0,
      "maxRetries": 3,
      "nextRetryAt": null,
      "processedAt": "2024-01-15T10:01:23Z",
      "metadata": {}
    },
    {
      "id": "item-002",
      "projectPath": "group/project2",
      "filePath": "src/LargeFile.kt",
      "status": "FAILED",
      "error": {
        "code": "TIMEOUT",
        "message": "Request timeout after 30s",
        "retryable": true,
        "occurredAt": "2024-01-15T10:02:15Z"
      },
      "retryCount": 2,
      "maxRetries": 3,
      "nextRetryAt": "2024-01-15T10:05:00Z",
      "processedAt": null,
      "metadata": {}
    }
  ],
  "metadata": {}
}
```

#### 2. project_states 컬렉션 (프로젝트 상태 추적)

```json
{
  "projectId": 123,
  "projectPath": "group/project1",
  "repositoryUrl": "https://gitlab.example.com/group/project1.git",
  "versionInstant": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### ERD 다이어그램

```
┌─────────────────────────────────────────────────────────┐
│                    jobs Collection                       │
├─────────────────────────────────────────────────────────┤
│ _id: String (PK)                                        │
│ sourceType: String                                      │
│ sourceConfig: Object                                    │
│   ├─ baseUrl: String                                   │
│   ├─ accessToken: String                               │
│   ├─ projectIds: Array<Long>                           │
│   ├─ groupIds: Array<Long>                             │
│   └─ ...                                                │
│ options: Object                                         │
│   ├─ mode: String (FULL|INCREMENTAL)                   │
│   ├─ fileFilters: Object                               │
│   └─ concurrency: Object                               │
│ status: String                                          │
│   (PENDING|PROCESSING|SUCCESS|PARTIAL_SUCCESS|         │
│    FAILED|CANCELLED|RETRYING)                          │
│ progress: Object                                        │
│   ├─ totalProjects: Number                             │
│   ├─ processedProjects: Number                         │
│   ├─ totalFiles: Number                                │
│   ├─ processedFiles: Number                           │
│   └─ failedFiles: Number                               │
│ createdAt: Date                                         │
│ startedAt: Date?                                        │
│ completedAt: Date?                                      │
│ cancelledAt: Date?                                      │
│ callbackUrl: String?                                    │
│ items: Array<JobItemDocument>                           │
│   ├─ id: String                                        │
│   ├─ projectPath: String                               │
│   ├─ filePath: String                                  │
│   ├─ status: String                                    │
│   ├─ error: Object?                                    │
│   ├─ retryCount: Number                                │
│   └─ ...                                               │
│ metadata: Object                                        │
└─────────────────────────────────────────────────────────┘
                            │
                            │ (논리적 관계)
                            │ (JobItem의 projectPath와 연결)
                            │
┌─────────────────────────────────────────────────────────┐
│              project_states Collection                    │
├─────────────────────────────────────────────────────────┤
│ projectId: Long (PK)                                     │
│ projectPath: String                                      │
│ repositoryUrl: String                                   │
│ versionInstant: String (ISO 8601)                       │
│ updatedAt: String (ISO 8601)                            │
└─────────────────────────────────────────────────────────┘
```

### 관계 설명

1. **jobs와 project_states의 관계**
   - 논리적 관계 (MongoDB는 관계형 DB가 아니므로 외래키 없음)
   - `jobs.items[].projectPath`와 `project_states.projectPath`로 연결
   - `project_states`는 증분 업데이트를 위한 프로젝트 버전 추적 정보

2. **jobs 내부 구조**
   - `items` 배열은 임베드된 문서 (Embedded Document)
   - 하나의 Job은 여러 JobItem을 포함
   - JobItem은 파일 단위 처리 정보를 담음

### 인덱스 설계

```javascript
// jobs 컬렉션 인덱스
db.jobs.createIndex({ status: 1, createdAt: -1 });
db.jobs.createIndex({ createdAt: -1 });

// project_states 컬렉션 인덱스
db.project_states.createIndex({ projectId: 1 }, { unique: true });
db.project_states.createIndex({ projectPath: 1 });
```

### 데이터 모델 특징

1. **정규화 최소화**
   - JobItem을 Job에 임베드하여 조회 성능 향상
   - 프로젝트 상태는 별도 컬렉션으로 분리 (증분 업데이트 최적화)

2. **유연한 스키마**
   - MongoDB의 스키마리스 특성 활용
   - `metadata` 필드로 확장 가능한 정보 저장

3. **타임스탬프 관리**
   - 모든 시간 정보는 ISO 8601 형식 문자열로 저장
   - 애플리케이션 레벨에서 Instant로 변환

4. **상태 추적**
   - Job 상태는 Enum 값으로 저장
   - 상태 전이 추적 가능

---

## 📈 확장성 고려사항

### 수평 확장

- **작업 큐**: Redis/RabbitMQ로 전환 시 여러 인스턴스에서 작업 공유 가능
- **MongoDB**: Replica Set 구성으로 읽기 성능 향상
- **로드 밸런서**: 여러 API 인스턴스 앞에 배치

### 수직 확장

- **동시성 제한**: `concurrency.projects`, `concurrency.files` 조정
- **메모리**: 대규모 프로젝트 처리 시 힙 메모리 증가
- **디스크**: 임시 저장소 공간 확보

---

## 🔒 보안 고려사항

1. **인증/인가**: API Key 기반 인증 (향후 구현)
2. **자격 증명**: Access Token은 환경 변수로 관리
3. **네트워크**: HTTPS 통신 필수
4. **데이터 보안**: 민감 정보 필터링 (`.env` 파일 등). MongoDB URI는 로그에 비밀번호가 마스킹되어 출력됨 (`MongoConfig`).

---
