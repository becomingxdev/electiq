<![CDATA[<div align="center">

# ⚡ ElectIQ

### *AI-Powered Election Assistant*

**Know your vote. Understand your rights. Stay informed.**

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Firebase-orange?style=for-the-badge&logo=firebase)](https://electiq-devdesai.web.app)
[![Backend](https://img.shields.io/badge/Backend-Cloud%20Run-blue?style=for-the-badge&logo=google-cloud)](https://electiq-backend-109778663813.asia-south1.run.app/api/v1/health)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

</div>

---

## 🗳️ Chosen Vertical — Civic Tech / Election Literacy

India has one of the world's largest democratic electorates — over 960 million registered voters — yet voter awareness and participation remain uneven. First-time voters often don't know if they're eligible, when registration closes, or how to cast a ballot correctly.

**ElectIQ** sits at the intersection of AI and civic technology. The goal is simple: remove every friction point that stands between a citizen and their vote, through a conversational, accessible interface that works on any device.

---

## 🧠 Approach & Logic

### The Problem
Election information in India is scattered across multiple government portals, PDFs, and local notices — often in formal language that intimidates first-time voters. Key pain points:

| Pain Point | Reality |
|---|---|
| Eligibility confusion | Citizens unsure if age, citizenship, or ID requirements apply to them |
| Date fragmentation | Registration deadlines, polling dates, and result dates live on different pages |
| No conversational interface | Official sites are form-heavy, not question-friendly |
| AI hallucination risk | General-purpose LLMs confidently fabricate specific election dates |

### The Solution Philosophy

ElectIQ uses a **tiered response strategy** designed to be fast, accurate, and cost-efficient:

```
User Question
     │
     ▼
┌─────────────────────────────────────────────┐
│  1. GUARD — Is this election-related?        │
│     → No  → Politely refuse                 │
│     → Yes → continue ↓                      │
└─────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────┐
│  2. CACHE — Was this already answered?       │
│     → Yes → Return instantly (free)         │
│     → No  → continue ↓                      │
└─────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────┐
│  3. STATIC ROUTER — Known pattern match?     │
│     registration / voter ID / NOTA / booth   │
│     → Yes → Hardcoded authoritative answer   │
│     → No  → continue ↓                      │
└─────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────┐
│  4. GEMINI — Complex or novel question       │
│     → gemini-2.5-flash with a strict system  │
│        prompt (≤60 words, no date guessing)  │
└─────────────────────────────────────────────┘
```

This architecture means **~80% of queries never reach Gemini**, reducing latency and API cost while eliminating the most common source of hallucination (fabricated election dates).

---

## 🏗️ How the Solution Works

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER BROWSER                            │
│                                                                 │
│   React 19 + Vite + Tailwind CSS + React Router v7              │
│   ┌────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│   │ Eligibility│  │  Election    │  │    AI Assistant      │   │
│   │  Checker   │  │  Timeline    │  │   (Chat Interface)   │   │
│   └────────────┘  └──────────────┘  └──────────────────────┘   │
│            │              │                    │                │
│            └──────────────┼────────────────────┘                │
│                     Axios + Interceptors                         │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTPS REST  (prod)
                              │ Vite Proxy  (local dev)
┌─────────────────────────────▼───────────────────────────────────┐
│               GOOGLE CLOUD RUN  (asia-south1)                   │
│                                                                 │
│   Spring Boot 3.4 · Java 17 · Context Path: /api/v1            │
│                                                                 │
│   POST /eligibility/check    →  EligibilityService              │
│   GET  /elections/timeline   →  ElectionService                 │
│   POST /assistant/ask        →  AssistantService                │
│   GET  /health               →  HealthController                │
│                                                                 │
│   Spring Security  │  CORS Config  │  Global Exception Handler  │
└────────────┬────────────────────────────────────────────────────┘
             │
             ▼ Only for complex questions
┌────────────────────────────┐    ┌──────────────────────────┐
│  Google Gemini 2.5 Flash   │    │  Firebase Firestore       │
│  (generativelanguage API)  │    │  · feedback collection   │
│  Low-temp, capped tokens   │    │  · usage_stats counters  │
└────────────────────────────┘    └──────────────────────────┘
```

### Frontend (React + Vite)

| File | Purpose |
|---|---|
| `src/pages/Dashboard.jsx` | Landing page with three feature cards |
| `src/pages/EligibilityChecker.jsx` | Form-driven eligibility check with instant feedback |
| `src/pages/ElectionTimeline.jsx` | State-based timeline lookup showing registration, polling, and result dates |
| `src/pages/AIAssistant.jsx` | Full chat interface with message history, typing indicator, and per-message feedback |
| `src/api/axios.js` | Axios instance with timeout, dev-mode logging, and normalized error interceptor |
| `src/api/electiqService.js` | Typed wrappers for all three backend API calls |
| `src/firebase.js` | Firestore client for feedback and usage analytics |

**Local dev proxy:** Vite proxies all `/api/v1/*` requests to `localhost:8080`, eliminating CORS issues entirely during development. In production builds, Axios calls the Cloud Run URL directly.

### Backend (Spring Boot)

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4.5 |
| Language | Java 17 |
| Security | Spring Security (CSRF disabled, stateless, CORS via env-configurable origin list) |
| Validation | Jakarta Validation (`@Valid`, `@Min`, `@Max`) |
| HTTP Client | `RestTemplate` with 5s connect / 10s read timeout |
| Boilerplate | Lombok |
| Deployment | Docker → Google Cloud Run |

#### API Endpoints

```
POST /api/v1/eligibility/check
  Body: { age: int, citizen: bool, hasIdProof: bool }
  Returns: { eligible: bool, message: string }

GET /api/v1/elections/timeline?state=<name>
  Returns: { state, registrationDeadline, pollingDate, resultDate }

POST /api/v1/assistant/ask
  Body: { question: string }
  Returns: { answer: string }

GET /api/v1/health
  Returns: "ElectIQ Backend Running"
```

#### AI Assistant — Smart Routing Detail

The `AssistantService` applies keyword matching before any API call:

- **Election keyword filter:** `vote`, `election`, `ballot`, `candidate`, `democracy`, `mla`, `mp`, `nota`, `parliament` and more
- **Timeline trigger patterns:** "next election", "election date", "when is election", plus `[state name] + election`
- **Static responses cover:** Voter registration, how to vote (EVM), voter ID alternatives, polling booth lookup, NOTA explanation, types of elections
- **Gemini system prompt:** Caps output at 150 tokens, temperature 0.2, instructs the model never to guess election dates
- **Rate limiting:** In-memory counter resets every 60 seconds (30 req/min global cap)
- **Response cache:** In-memory `ConcurrentHashMap` — same normalized query never hits Gemini twice within the minute window

### Firebase Firestore

Used for lightweight, write-only analytics — no auth required:

| Collection | Purpose | Rules |
|---|---|---|
| `feedback` | Stores thumbs-up/down on assistant replies | Write-only (no read/delete) |
| `usage_stats` | Atomic counter of total assistant queries | Increment-only |
| `faqs` | Reserved for future public FAQ storage | Read-only |

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Java | 17 |
| Maven | 3.9+ (or use `./mvnw`) |
| Node.js | 18+ |
| npm | 9+ |

### Run Locally

**1. Clone the repository**
```bash
git clone https://github.com/becomingxdev/electiq.git
cd electiq
```

**2. Start the backend**
```bash
cd backend

# Create backend/.env with your Gemini key
echo "GEMINI_API_KEY=your_key_here" > .env

./mvnw spring-boot:run
# → Listening on http://localhost:8080
# → API base: http://localhost:8080/api/v1
```

**3. Start the frontend**
```bash
cd frontend
npm install
npm run dev
# → App at http://localhost:5173
# → API calls proxied to http://localhost:8080/api/v1
```

> No CORS configuration needed for local development — Vite's dev proxy handles it transparently.

### Environment Variables

#### Frontend (`frontend/.env`)
```env
# Local dev — Vite proxies this path to VITE_PROXY_TARGET
VITE_API_BASE_URL=/api/v1
VITE_PROXY_TARGET=http://localhost:8080
```

#### Frontend (`frontend/.env.production`)
```env
# Production — full Cloud Run URL (used by vite build automatically)
VITE_API_BASE_URL=https://<your-cloudrun-service>.run.app/api/v1
```

#### Backend (Cloud Run environment variables)
```env
GEMINI_API_KEY=your_gemini_api_key
ALLOWED_ORIGINS=https://your-app.web.app,https://your-app.firebaseapp.com
```

---

## ☁️ Deployment

### Frontend → Firebase Hosting

```bash
cd frontend
npm run build              # Picks up .env.production automatically
firebase deploy --only hosting
```

### Backend → Google Cloud Run

```bash
cd backend

# Build and push the Docker image
gcloud builds submit --tag gcr.io/YOUR_PROJECT_ID/electiq-backend

# Deploy to Cloud Run
gcloud run deploy electiq-backend \
  --image gcr.io/YOUR_PROJECT_ID/electiq-backend \
  --region asia-south1 \
  --platform managed \
  --allow-unauthenticated \
  --set-env-vars "GEMINI_API_KEY=your_key,ALLOWED_ORIGINS=https://your-app.web.app"
```

---

## 📋 Assumptions Made

1. **Indian electoral context only.** The eligibility logic (age 18+, citizenship, ID proof) and the AI system prompt are calibrated for Indian elections. The static keyword list includes Indian-specific terms (`mla`, `mp`, `nota`, `panchayat`).

2. **Election timeline data is mocked.** The `ElectionService` returns placeholder dates (`2026-09-01` for registration, `2026-09-20` for polling, `2026-09-25` for results) for any state. Integrating with the ECI's live data feed is a natural next step.

3. **Stateless API — no authentication.** This is a public-facing informational tool with no user accounts or protected resources. All API endpoints are open.

4. **Rate limiting is in-memory and per-instance.** The 30 req/min cap resets every 60 seconds and is not distributed across Cloud Run instances. Under high load, each instance enforces its own limit independently.

5. **Feedback is anonymous.** Firestore feedback documents are write-only with no IP tracking or user identification — by design.

6. **Gemini API key is required for complex queries only.** The system degrades gracefully: static responses still work without a valid key; only novel questions fall back to a "temporarily unavailable" message.

7. **Response caching is ephemeral.** The in-memory `ConcurrentHashMap` cache clears every 60 seconds and does not survive instance restarts. A Redis cache would be the production upgrade path.

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Frontend Framework | React 19 |
| Build Tool | Vite 8 |
| Styling | Tailwind CSS 3 |
| Routing | React Router v7 |
| HTTP Client | Axios 1.x |
| Backend Framework | Spring Boot 3.4.5 |
| Backend Language | Java 17 |
| AI Provider | Google Gemini 2.5 Flash |
| Frontend Hosting | Firebase Hosting |
| Backend Hosting | Google Cloud Run (asia-south1) |
| Database | Firebase Firestore (analytics only) |
| Containerization | Docker (multi-stage, Alpine JRE) |

---

## 📁 Project Structure

```
electiq/
├── backend/
│   ├── src/main/java/com/electiq/backend/
│   │   ├── config/         # SecurityConfig (CORS), RestTemplateConfig
│   │   ├── controller/     # EligibilityController, ElectionController,
│   │   │                   # AssistantController, HealthController
│   │   ├── dto/            # Request/Response POJOs
│   │   ├── exception/      # GlobalExceptionHandler
│   │   └── service/        # Business logic + Gemini integration
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/            # axios.js (interceptors), electiqService.js
│   │   ├── pages/          # Dashboard, EligibilityChecker,
│   │   │                   # ElectionTimeline, AIAssistant
│   │   ├── firebase.js     # Firestore client
│   │   └── App.jsx         # Router + layout shell
│   ├── .env                # Local dev config
│   ├── .env.production     # Production config (Cloud Run URL)
│   └── vite.config.js      # Includes dev proxy for /api/v1
│
├── firebase.json           # Hosting + Firestore config
├── firestore.rules         # Write-only feedback & stats rules
└── README.md
```

---

## 🔮 Future Roadmap

- [ ] **Live ECI data integration** — Fetch real election schedules from the Election Commission of India's public API
- [ ] **Multi-language support** — Hindi, Tamil, Bengali, and other regional languages
- [ ] **Constituency lookup** — Enter PIN code to find your booth and representative
- [ ] **Push notifications** — Remind users of upcoming registration deadlines
- [ ] **Distributed caching** — Redis on Cloud Memorystore for shared response cache across Cloud Run instances
- [ ] **Admin dashboard** — View aggregated usage stats and feedback from Firestore

---

## 👤 Author

Built for **PromptWars Hackathon — Challenge 2**  
by [@becomingxdev](https://github.com/becomingxdev)

---

<div align="center">

*"Democracy is not a spectator sport."*

**[🌐 Try it live →](https://electiq-devdesai.web.app)**

</div>
]]>