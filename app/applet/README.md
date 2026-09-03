# PromptForge ⚡

PromptForge is a modern Android application for prompt engineering, structured prompt composition, real-time Gemini AI simulation, and systematic prompt evaluations.

---

## ✨ Features

- **🎯 Prompt Studio & Live Simulation**
  - Interactive prompt editor with real-time token estimation (`~4 chars / token` heuristic).
  - One-tap Gemini response generation with low-latency streaming simulation.
  - **Comprehensive Execution Metrics**:
    - **Response Latency**: Precise execution timing in milliseconds (`ms`) or seconds (`s`).
    - **Token Usage Breakdown**: Input / Prompt Tokens, Output / Candidate Tokens, and Total Tokens (using official Gemini `usageMetadata` or token estimation).
    - **Generation Throughput**: Real-time throughput calculations in tokens per second (`tok/s`).
    - Character and word counts for inputs and outputs.
    - One-tap copy to clipboard and save to history runs.

- **🧩 Framework Composer**
  - Rapid scaffolding using battle-tested prompt engineering frameworks:
    - **GEPA** (Genetic-Pareto optimization loop based on execution traces and multi-objective criteria)
    - **CO-STAR** (Context, Objective, Style, Tone, Audience, Response)
    - **CRAFT** (Context, Role, Action, Format, Tone)
    - **RTF** (Role, Task, Format)
    - **Freeform** (Unconstrained playground)
  - Reactive live compilation and instant AI prompt rewriting ("Improve with AI").

- **🧪 Eval Lab (A/B Testing & LLM-as-a-Judge)**
  - Systematic A/B prompt evaluation across multiple configurable test cases.
  - Concurrent / Parallel execution (`coroutineScope` + `async`/`awaitAll`).
  - **Categorized Autoeval Judges**:
    - *Generic LLM-as-a-Judge*
    - *Factuality & Hallucination Resistance (Braintrust-style)*
    - *Conciseness & Precision*
    - *Toxicity & Safety*
    - *Strict Format Validation*

- **📚 Library & History**
  - Offline-first local storage using Android **Room Database**.
  - Persist, edit, and reload favorite prompts and playground execution traces.

---

## 🛠️ Tech Stack & Architecture

- **UI**: 100% Kotlin Jetpack Compose with Material Design 3 (Dynamic Light & Dark Theme).
- **Architecture**: MVVM with Kotlin Coroutines & reactive `StateFlow`.
- **Local Persistence**: Room SQLite Database with Type-Safe DAOs.
- **Networking**: Retrofit 2 + OkHttp + Kotlinx Serialization (`application/json`).
- **AI Integration**: Google Gemini API via secure `x-goog-api-key` header authentication.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug | 2024.2+ or Android CLI tools with JDK 21.
- Gemini API Key (obtain from [Google AI Studio](https://aistudio.google.com/)).

### Configuration
Set your Gemini API key in your environment before building:

```bash
export GEMINI_API_KEY="your_api_key_here"
```

In Google AI Studio or CI/CD pipelines, configure `GEMINI_API_KEY` in the Secrets / Environment panel.

### Build and Run

To assemble the debug APK:
```bash
./gradlew assembleDebug
```

To run unit and local Robolectric tests:
```bash
./gradlew test
```
