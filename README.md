<div align="center">

<h1>🌍 GeoSphere</h1>
<p><strong>A geography quiz app that challenges your knowledge of the world</strong></p>

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue?logo=kotlin)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

</div>

---

## 📸 Overview

GeoSphere is a fully-featured Android quiz app focused on world geography. Test yourself on flags, capitals, landmarks, continents, and more — compete on live leaderboards and unlock milestone achievements as you grow.

---

## ✨ Features

### 🧠 Quiz Engine
- **8 categories**: World, Asia, Europe, Americas, Africa, Flags, Capitals, Landmarks
- Timed questions (30 seconds each) with animated countdown bar
- Explanations shown after each answer
- Points awarded per correct answer (easy = 1pt, hard = 2pt)
- Options locked after submit to prevent re-selection

### 🏆 Leaderboard
- **3 tabs**: Daily · Weekly · All Time
- Scores automatically reset at midnight (daily) and Monday (weekly)
- Medal emojis 🥇🥈🥉 for top 3 players
- Real-time data from Firebase Realtime Database

### 🎖️ Achievements
Milestone-based system — unlock badges as your total correct answers grow:

| Badge | Name | Required |
|-------|------|----------|
| 🌱 | Just Started | 5 correct |
| 🌿 | Transforming Beginner | 10 correct |
| 🗺️ | Getting Serious | 25 correct |
| 🌍 | Geo Geek | 50 correct |
| 🏆 | You're Here to Stay | 100 correct |
| 🌐 | World Explorer | 250 correct |
| 🎓 | Geography Master | 500 correct |
| 👑 | Globe Trotter Legend | 1000 correct |

### 🎨 Themes
5 built-in color themes switchable from Settings:

| Theme | Primary | Accent |
|-------|---------|--------|
| 🌑 Midnight (default) | `#2C3E50` | `#1ABC9C` |
| 🌊 Ocean | `#1A3A5C` | `#00BCD4` |
| 🌅 Sunset | `#2D2D2D` | `#E67E22` |
| 🌿 Forest | `#1B3A1F` | `#4CAF50` |
| 👑 Royal | `#2D1B4E` | `#FFD700` |

### ⚙️ Settings
- Sound effects & animations toggles
- App theme picker (with live restart)
- Send feedback via email
- Rate & share the app
- About section (developer info, version, links)

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | XML Layouts + ViewBinding + Material Design 3 |
| Architecture | Activity-based with Coroutines |
| Auth | Firebase Authentication (Email + Google Sign-In) |
| Database | Firebase Realtime Database |
| Async | Kotlin Coroutines + `lifecycleScope` |
| Theme | Custom `BaseActivity` + `ThemeHelper` + SharedPreferences |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11+
- A Firebase project with **Realtime Database** and **Authentication** enabled

### 1. Clone & Open
```bash
git clone https://github.com/imrancodesfr/GeoSphere.git
cd GeoSphere
```
Open the project in Android Studio.

### 2. Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com) → Create project
2. Add an Android app with package `com.example.geosphere`
3. Download `google-services.json` and place it in `app/`
4. Enable **Email/Password** and **Google** sign-in under Authentication
5. Create a **Realtime Database** (start in locked mode)

### 3. Import Database Rules
In Firebase Console → Realtime Database → **Rules** tab, paste the contents of [`firebase_database_rules.json`](firebase_database_rules.json) and click **Publish**.

### 4. Seed Questions
```bash
# Option A: Use the included seed file (160 hand-crafted questions)
# Firebase Console → Realtime Database → ⋮ → Import JSON → select firebase_seed.json

# Option B: Fetch from Open Trivia DB (100+ questions per run, free)
python3 fetch_trivia.py
# Then import the generated trivia_questions.json into Firebase
```

### 5. Build & Run
```bash
./gradlew assembleDebug
```
Or press ▶ Run in Android Studio.

---

## 📁 Project Structure

```
app/src/main/java/com/example/geosphere/
├── activities/
│   ├── BaseActivity.kt          # Theme application before view inflation
│   ├── SplashActivity.kt
│   ├── LoginActivity.kt
│   ├── RegisterActivity.kt
│   ├── MainActivity.kt          # Home with category grid
│   ├── CategoryActivity.kt
│   ├── QuizActivity.kt          # Core quiz engine
│   ├── ResultActivity.kt        # Score + achievements
│   ├── LeaderboardActivity.kt   # Daily / Weekly / All-Time tabs
│   ├── AchievementsActivity.kt  # Progress + locked milestones
│   ├── SettingsActivity.kt      # Theme picker, feedback, about
│   ├── AdminLoginActivity.kt
│   └── AdminDashboardActivity.kt
├── adapters/
│   ├── LeaderboardAdapter.kt
│   ├── AchievementsAdapter.kt
│   └── CategoryAdapter.kt
├── models/
│   ├── User.kt
│   ├── Question.kt              # options: List<String>
│   ├── LeaderboardEntry.kt      # includes daily/weekly fields
│   └── Achievement.kt
└── utils/
    ├── FirebaseHelper.kt        # All Firebase operations
    ├── ThemeHelper.kt           # 5 theme definitions + persistence
    ├── AchievementMilestones.kt # 8 milestone definitions
    └── Constants.kt
```

---

## 🤝 Adding More Questions

### Via Python Script (recommended)
```bash
python3 fetch_trivia.py   # fetches 100–150 questions from opentdb.com
# Then: Firebase Console → Import JSON → trivia_questions.json
```

### Firebase JSON Format
```json
{
  "questions": {
    "world": {
      "q001": {
        "questionText": "What is the largest country by area?",
        "options": { "0": "China", "1": "Russia", "2": "Canada", "3": "USA" },
        "correctOptionIndex": 1,
        "explanation": "Russia spans over 17 million km².",
        "difficulty": "easy",
        "points": 1
      }
    }
  }
}
```
> Options **must** use string keys `"0"–"3"` — the app sorts them by key to preserve answer order.

---

## 👤 Developer

**Imran Khan**
- GitHub: [@imrancodesfr](https://github.com/imrancodesfr)
- Email: imrancodesfr@gmail.com

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
Made with ❤️ and ☕ • © 2025–2026 Imran Khan
</div>
