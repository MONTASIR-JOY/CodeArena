# CodeArena: A Real-Time Virtual Contest Simulator, Multi-Platform Practice Tracker, and AI Coach for Competitive Programmers

**Course:** CSE 2200 Advanced Programming Laboratory  
**Package:** `com.kuet.codearena`  
**Toolchain:** Android Studio Dolphin (2021.3.1) | AGP 7.3.1 | Gradle 7.4 | JDK 11 | compileSdk 33 | minSdk 24  
**Language:** Pure Java (1.8 Source & Target) — Zero Kotlin, Zero Jetpack Compose  

---

## 🚀 Overview

CodeArena is a comprehensive native Android application designed for competitive programmers. It unifies training workflows across **Codeforces**, **AtCoder**, **LeetCode**, and **CodeChef**:

1. **Virtual Contest Simulator:** Full contest reproduction with live countdown via ForegroundService, submission tracking against official platform APIs, halfway & 10-minute alerts, and post-mortem penalty scoring.
2. **Spaced-Repetition Upsolve Tracker:** Enqueues unsolved contest problems into a 1, 3, 7, 14-day repetition cycle powered by exact `AlarmManager` wakeups and `BootReceiver` survival.
3. **Multi-Platform Solve Aggregator:** Ingests accepted submissions across platforms, updating an internal ELO-based Practice Rating and daily streak counters.
4. **Tag Mastery & Visual Analytics:** Interactive radar charts of category strengths, weakness bar charts, contest performance score graphs, and XML-parsed algorithmic tips.
5. **Rule & AI Coaching Engine:** Rule-based 5-rung progressive ladders and 7-day contest participation strategies paired with Google Gemini REST API explanations.
6. **Global Leaderboards & ContentProvider:** Firestore-synced multi-window rankings and a read-only `ContentProvider` for external data inspection.

---

## 🛠️ Toolchain & Compatibility

CodeArena is tailored specifically for **Android Studio Dolphin (2021.3.1)**:

* **Android Gradle Plugin (AGP):** `7.3.1`
* **Gradle Wrapper:** `7.4`
* **Java:** OpenJDK 11 (`sourceCompatibility = JavaVersion.VERSION_1_8`, `targetCompatibility = JavaVersion.VERSION_1_8`)
* **SDK:** `compileSdk = 33`, `targetSdk = 33`, `minSdk = 24`
* **UI Framework:** Android ViewBinding enabled (`viewBinding = true`), Material Components 1.6.1, ConstraintLayout, MPAndroidChart v3.1.0

---

## 🔑 Configuration & API Keys

All external API secrets are loaded into `BuildConfig` via `local.properties` (which is excluded in `.gitignore`):

```properties
## local.properties
sdk.dir=C\:\\Users\\YourUser\\AppData\\Local\\Android\\Sdk

# Optional: Google Gemini API Key for AI Coaching insights
GEMINI_API_KEY=your_gemini_api_key_here

# Optional: CLIST.by Calendar API credentials
CLIST_USERNAME=your_clist_username
CLIST_API_KEY=your_clist_api_key
```

> **Note on Graceful Fallback:** If any API key is missing or unconfigured:
> * Gemini AI falls back seamlessly to the built-in offline **Rule-Based Engine**.
> * CLIST.by falls back to direct platform public endpoints (Codeforces, AtCoder, LeetCode).
> * The app functions fully with zero crashes.

---

## 🔥 Firebase Setup & Security Rules

### 1. Firebase Project Setup
1. Open the [Firebase Console](https://console.firebase.google.com/) and create a project named `CodeArena`.
2. Register an Android app with package name: `com.kuet.codearena`.
3. Add your debug SHA-1 signing fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
4. Download `google-services.json` and place it in the `app/` directory (`d:\codearena\app\google-services.json`).
5. Enable **Google Sign-In** under *Authentication > Sign-in method*.
6. Enable **Cloud Firestore** in test or production mode.

### 2. Cloud Firestore Security Rules
Paste the following rules in the Firebase Console under *Firestore Database > Rules*:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Any authenticated user can read global leaderboards
    match /users/{userId} {
      allow read: if request.auth != null;
      // Users can only create or update their own profile & metrics
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

If `google-services.json` is missing or unconfigured, the app detects this at startup via `CodeArenaApp.isFirebaseAvailable()`, disables Google Sign-In with a friendly banner, and provides offline competitive leaderboards.

---

## 📡 ContentProvider Specification

CodeArena exposes a read-only `ContentProvider` for inter-app communication and inspection of pending upsolve items.

* **Authority:** `com.kuet.codearena.provider`
* **Content URI:** `content://com.kuet.codearena.provider/upsolve`
* **MIME Type:** `vnd.android.cursor.dir/vnd.com.kuet.codearena.upsolve`

### Exported Columns:
| Column | Type | Description |
|---|---|---|
| `_id` | `INTEGER` | Primary key |
| `contest_id` | `INTEGER` | Codeforces contest identifier |
| `problem_index` | `TEXT` | Problem index (`A`, `B`, `C`...) |
| `problem_name` | `TEXT` | Title of the problem |
| `rating` | `INTEGER` | Difficulty rating |
| `interval_days` | `INTEGER` | Spaced repetition interval (1, 3, 7, 14) |
| `due_date` | `INTEGER` | Epoch timestamp (ms) when solve is due |
| `is_solved` | `INTEGER` | `1` if completed, `0` if pending |

### Query Example (External App / Test):
```java
Uri uri = Uri.parse("content://com.kuet.codearena.provider/upsolve");
Cursor cursor = getContentResolver().query(uri, null, null, null, null);
while (cursor != null && cursor.moveToNext()) {
    String name = cursor.getString(cursor.getColumnIndexOrThrow("problem_name"));
    int rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating"));
}
```

---

## 📋 Evaluation Checklist & Feature Map

| Syllabus Topic | Implementation Class / Resource | Demo Location |
|---|---|---|
| **Activity Lifecycle** | `VirtualRunActivity.java` | 8 lifecycle callbacks logged; survives configuration changes |
| **Foreground Service & Notifications** | `VirtualContestService.java` | Running notification with live timer and tick broadcasts |
| **BroadcastReceivers** | `AlarmReceiver.java`, `BootReceiver.java` | AlarmManager triggers + reboot alarm re-registration |
| **AlarmManager Exact Alarms** | `AlarmScheduler.java` | Exact RTC_WAKEUP alarms with Android 12+ permission handling |
| **Room Local Database** | `AppDatabase.java`, 7 Entities, 7 DAOs | Offline caching of contests, runs, solves, and statistics |
| **Retrofit + Rate Limiter** | `ApiClient.java`, `RateLimiter.java` | Strict 2.0s Codeforces rate limiter + Kenkoooo + LeetCode |
| **Custom Canvas View** | `CircularTimerView.java`, `attrs.xml` | Canvas ring timer on Virtual Run & Home Daily Target |
| **XML Pull Parser** | `XmlTipParser.java`, `tag_tips.xml` | Parses offline tactical advice from raw XML |
| **Implicit Intents** | `PostMortemActivity.java`, `CalendarFragment.java` | Share score (ACTION_SEND), Browser problem view, Add to Calendar |
| **RecyclerView & DiffUtil** | `ContestAdapter.java`, `ProblemAdapter.java`, etc. | Custom ViewHolders with DiffUtil list adapters |
| **ContentProvider** | `UpsolveContentProvider.java` | `content://com.kuet.codearena.provider/upsolve` |
| **Firebase Auth & Firestore** | `AuthManager.java`, `FirestoreRepository.java` | Google Sign-In credential exchange and Firestore sync |

