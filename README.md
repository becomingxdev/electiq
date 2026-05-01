<div align="center">

# ⚡ ElectIQ

### *Empowering Democracy Through AI-Driven Civic Literacy*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Vertex AI](https://img.shields.io/badge/Vertex%20AI-Gemini%202.5%20Flash-blue?style=flat-square&logo=google-cloud)](https://cloud.google.com/vertex-ai)
[![Cloud Run](https://img.shields.io/badge/Cloud%20Run-asia--south1-blue?style=flat-square&logo=google-cloud)](https://cloud.google.com/run)
[![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-green?style=flat-square&logo=swagger)](http://localhost:8080/swagger-ui/index.html)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

> An AI-powered assistant that demystifies the election process — from voter registration to result day — in a conversational, citizen-first experience.

</div>

---

## 🗳️ Chosen Vertical: Election Process Education (Civic Tech)

**ElectIQ** targets the **Election Process Education** vertical.

Across India's massive electorate, a "participation barrier" quietly silences millions of eligible voters:

| Challenge | Real-World Impact |
|-----------|-------------------|
| 📅 Missed registration deadlines | Citizens find out too late; miss election rolls |
| 📄 Process confusion | Voters unsure of valid ID types or polling booth rules |
| 🏛️ Portal fragmentation | Critical information buried across 15+ government websites |
| 🌐 Language & literacy gaps | Complex electoral language inaccessible to first-time voters |

**ElectIQ solves this** by creating a single, conversational interface that answers the questions every citizen deserves to have answered — accurately, instantly, and without jargon.

---

## 🧠 Approach & Logic: "Deterministic-First, Generative-Second"

The biggest risk in building an electoral assistant is **AI hallucination on critical facts** — wrong polling dates or incorrect eligibility rules can suppress voter participation. ElectIQ's core design philosophy directly addresses this.

### The Architecture Principle

> *"Use generative AI to explain; use authoritative data to inform."*

ElectIQ never lets Gemini guess a date. Factual lookups always route to structured, curated datasets. The AI handles only natural language — nuance, explanation, and conversation.

### The Request Lifecycle

```
User Query
    │
    ▼
┌─────────────────────────────────────┐
│         1. Security Gate            │  ← API Key Filter (fail-closed)
└─────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────┐
│         2. Input Validation         │  ← Blank/null guard + sanitization
└─────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────┐
│         3. Domain Guard             │  ← Is this election-related?
│            Off-topic → REJECT        │    (Saves LLM calls + costs)
└─────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────┐
│         4. L1 Cache Lookup          │  ← In-Memory (ConcurrentHashMap + TTL)
│            HIT → Return instantly   │    Sub-millisecond response
└─────────────────────────────────────┘
    │ MISS
    ▼
┌─────────────────────────────────────┐
│         5. Static Intent Router     │  ← Deterministic routing for known patterns:
│                                     │    • "when is election in [state]?" → elections.json
│                                     │    • "what is NOTA?" → pre-authored response
│                                     │    • "voter registration?" → step-by-step guide
└─────────────────────────────────────┘
    │ UNMATCHED
    ▼
┌─────────────────────────────────────┐
│      6. Google Vertex AI (Gemini)   │  ← Singleton gRPC client (initialized once)
│         Gemini 2.5 Flash            │    Strict system prompt: no date hallucination
└─────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────┐
│         7. Cache Write              │  ← Store result for 5 min TTL
└─────────────────────────────────────┘
    │
    ▼
Response → User
```

---

## 🏗️ How the Solution Works

### 1. Backend Engine (Spring Boot 3.4.5)

Built on **Clean Architecture** — every layer has exactly one responsibility:

```
Controller → Service → Cache → Vertex AI
```

*   **`AssistantController`**: Accepts user queries via `POST /assistant/ask`, delegates everything to the service layer. Zero business logic.
*   **`AssistantService`**: Orchestrates intent detection, static routing, cache interaction, and AI delegation. All magic strings live in `AppConstants` — no inline literals.
*   **`ElectionService`**: Loads `elections.json` at startup into an `unmodifiableMap`. Thread-safe, zero I/O on subsequent calls.
*   **`EligibilityService`**: Pure Java logic for determining voter eligibility (age ≥ 18, citizenship, ID proof).
*   **`VertexAIService`**: Delegates to a pre-warmed singleton `GenerativeModel` bean — never creates a new connection per request.

### 2. AI Integration (Google Vertex AI)

| Property | Value |
|----------|-------|
| **SDK** | `google-cloud-vertexai` (official Java SDK) |
| **Model** | `gemini-2.5-flash` |
| **Client** | Singleton bean — gRPC channels reused across all requests |
| **Auth** | Application Default Credentials (ADC) — no hardcoded keys |
| **Region** | `asia-south1` (Mumbai) — lowest latency for Indian users |

**The System Prompt is deliberately strict:**
> *"You're ElectIQ, an election assistant. Answer only election questions in clear sentences under 60 words. If unrelated, refuse. Don't hallucinate dates. If unknown, advise checking official updates."*

### 3. Caching Layer

| Cache Type | Implementation | TTL | Scope |
|------------|----------------|-----|-------|
| **In-Memory** | `ConcurrentHashMap` with lazy expiry | 5 minutes | Single instance |
| **Redis-Ready** | `RedisCacheService` abstraction | N/A | Distributed (production) |

The active cache is selected at startup via `CACHE_TYPE=memory|redis` environment variable. Defaults to `memory`.

### 4. Security Model

*   **API Key Filter**: All endpoints (except `/health`, `/swagger-ui`, `/v3/api-docs`) require a valid `x-api-key` header.
*   **Fail-Closed by Default**: If the `API_KEY` environment variable is not set, *every* request is rejected with `401 Unauthorized`.
*   **Input Sanitization**: Jakarta Bean Validation (`@NotBlank`) is applied at the controller layer before any processing begins.
*   **No Secrets in Code**: All credentials injected via environment variables. Designed for GCP Secret Manager integration.

### 5. API Documentation (OpenAPI 3.0)

A fully interactive **Swagger UI** is available at `/swagger-ui/index.html`:
*   Authenticate once with your `x-api-key`.
*   Test all 3 endpoints live from the browser.
*   Inspect request/response schemas.

---

## 📋 Assumptions Made

1.  **Indian Electoral Context**: The application is calibrated for India — voter eligibility is defined as Age ≥ 18 + Citizenship. State names match the Indian political map.

2.  **`elections.json` as Ground Truth**: State-wise election timelines are stored in a curated JSON file and treated as authoritative. This prevents the AI from hallucinating dates and ensures deterministic, reproducible answers.

3.  **HTTPS in Production**: The `x-api-key` header is sensitive. The application assumes it is served over HTTPS in any environment beyond local development.

4.  **GCP Runtime Variables**: `GOOGLE_CLOUD_PROJECT` and `GOOGLE_CLOUD_LOCATION` are expected to be set as environment variables. This is standard for all Cloud Run deployments using ADC.

5.  **Single-Instance Caching for Prototype**: The `InMemoryCacheService` is appropriate for a single Cloud Run instance. For multi-instance production deployments, the `RedisCacheService` abstraction should be promoted to a full Google Cloud Memorystore implementation.

6.  **English Language Queries**: The current build assumes English-language input. Multilingual support (Hindi, Tamil, etc.) is architecturally possible with minor prompt modifications.

---

## 🧰 Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Framework** | Spring Boot 3.4.5 (Java 17) | Core application server |
| **AI SDK** | Google Cloud Vertex AI | Gemini 2.5 Flash integration |
| **Security** | Spring Security + `ApiKeyFilter` | Stateless API key auth |
| **Documentation** | SpringDoc OpenAPI (Swagger UI) | Interactive API explorer |
| **Caching** | `ConcurrentHashMap` + Redis Abstraction | Tiered response caching |
| **Testing** | JUnit 5 + Mockito + Spring WebMvcTest | Unit + slice testing |
| **Build** | Maven Wrapper (`mvnw`) | Reproducible builds |
| **Deployment** | Google Cloud Run | Serverless container hosting |
| **Logging** | SLF4J + Logback | Structured observability |
| **Config** | `spring-dotenv` + Environment Variables | Externalized configuration |

---

## 🚀 Getting Started

### Prerequisites

*   Java 17+
*   Maven 3.9+ (or use the included `mvnw` wrapper)
*   A Google Cloud project with Vertex AI API enabled
*   Application Default Credentials configured (`gcloud auth application-default login`)

### 1. Clone & Configure

```bash
git clone https://github.com/your-username/electiq.git
cd electiq
```

Create a `.env` file in the `backend/` directory:

```env
GOOGLE_CLOUD_PROJECT=your-gcp-project-id
GOOGLE_CLOUD_LOCATION=asia-south1
API_KEY=your-secure-api-key
CACHE_TYPE=memory
```

### 2. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API is now live at `http://localhost:8080/api/v1`

### 3. Explore the API

Open **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Click **Authorize** and enter your `API_KEY` value. You can now test all endpoints interactively.

### 4. Run Tests

```bash
./mvnw test
```

All **13 tests** should pass.

---

## 📡 API Endpoints

All endpoints are prefixed with `/api/v1` and require `x-api-key` header.

### `POST /assistant/ask`
The core AI conversation endpoint.

```json
// Request
{ "question": "How do I register to vote in Maharashtra?" }

// Response
{ "answer": "Visit nvsp.in or your local BLO office. Submit Form 6 with address proof and a passport-size photo before the nomination deadline." }
```

### `GET /elections/timeline?state=Maharashtra`
Deterministic, AI-free lookup from the elections dataset.

```json
{ "state": "Maharashtra", "timeline": "Elections scheduled for November 2024." }
```

### `POST /eligibility/check`
Rule-based eligibility determination.

```json
// Request
{ "age": 25, "citizen": true, "hasIdProof": true }

// Response
{ "eligible": true, "message": "You are eligible to vote." }
```

---

## 🔮 Future Roadmap: ElectIQ 2.0

- [ ] **Live ECI Grounding**: Vertex AI Function Calling to query real-time Election Commission of India APIs
- [ ] **Multi-Modal Support**: Upload a Voter ID photo to verify registration via Gemini Vision
- [ ] **Multilingual**: Hindi, Tamil, Telugu, and Bengali support for broader citizen reach
- [ ] **Hyper-Local Awareness**: Constituency and polling booth lookup via PIN code
- [ ] **Cloud Memorystore**: Promote the Redis abstraction to a full GCP Memorystore deployment
- [ ] **Rate Limiting**: Bucket4j integration to protect LLM cost budgets per key

---

<div align="center">

---

**ElectIQ — Empowering every vote, one conversation at a time.**

Built for **PromptWars Challenge 2** · Vertical: Election Process Education

*Optimized for Production. Hardened for Security. Designed for Citizens.*

</div>