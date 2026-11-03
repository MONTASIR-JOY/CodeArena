# Live Demonstration Guide & Technical Defense Notes
## Course: CSE 2200 Advanced Programming Laboratory

This document serves as your complete script for the live oral examination and project demonstration of **CodeArena**. Follow the timed walkthrough and review the 15 technical Q&A pairs before entering the viva.

---

## Part 1: 5-Minute Live Demonstration Script

| Time | Screen / Action | Key Features to Highlight to the Evaluator |
|---|---|---|
| **0:00 - 0:45** | **Splash & Authentication**<br>• Launch app from Android Studio.<br>• Show `LoginActivity`.<br>• Tap "Continue without account" (or Google Sign-In). | • Highlight Firebase Auth integration.<br>• Emphasize graceful fallback: if Firebase is not configured, a clear banner appears and local features work seamlessly.<br>• Explain the routing in `SplashActivity`. |
| **0:45 - 1:45** | **Virtual Contest Simulator**<br>• Open **Contests** tab.<br>• Filter by chip ("Div. 3").<br>• Tap a contest to open `ContestDetailActivity`.<br>• Tap "Start Virtual Run" -> `VirtualRunActivity`.<br>• Rotate the phone to demonstrate lifecycle preservation. | • Point out custom `CircularTimerView` Canvas ring.<br>• Show active `ForegroundService` ongoing notification in notification shade.<br>• Point to Logcat: show all 8 Activity Lifecycle callbacks logged (`onCreate` through `onDestroy`).<br>• Show live submission polling (15s interval via `RateLimiter`).<br>• Explain remaining time computation from `savedStartTimeMs` (survives process death). |
| **1:45 - 2:30** | **Post-Mortem & Share Intent**<br>• Tap "Finish Run" -> `PostMortemActivity`.<br>• Review score breakdown, penalty points, and problem cards.<br>• Tap "Share Summary". | • Show the mathematical scoring formula live on screen.<br>• Point to the implicit `Intent(Intent.ACTION_SEND)` chooser opening WhatsApp/Gmail/Messages.<br>• Return to Home: verify unsolved problems automatically entered the Spaced Repetition Upsolve Queue. |
| **2:30 - 3:30** | **Spaced Repetition & ContentProvider**<br>• On **Home** tab, show Daily Target ring.<br>• Tap "Verify" on an upsolve item.<br>• Tap problem title to show implicit browser intent.<br>• Explain `UpsolveContentProvider`. | • Explain the 1, 3, 7, 14-day exponential spacing schedule.<br>• Show `AlarmManager.setExactAndAllowWhileIdle()` and `BootReceiver` code.<br>• Quote the ContentProvider URI: `content://com.kuet.codearena.provider/upsolve`. |
| **3:30 - 4:15** | **Tag Mastery & AI Coaching**<br>• Navigate to **Practice** tab.<br>• Review "Solved Today" horizontal recycler.<br>• Show Top 8 Tag RadarChart & Weakness BarChart.<br>• Show XML-parsed algorithmic tip box.<br>• Show 5-step practice ladder and AI coaching advice. | • Point to `XmlPullParser` on `res/raw/tag_tips.xml`.<br>• Explain the ELO Practice Rating calculation.<br>• Show "AI-generated" badge from Gemini REST API or offline Rule Engine fallback.<br>• Demonstrate stepped ladder unlocking mechanism. |
| **4:15 - 5:00** | **Multi-Platform Calendar & Leaderboards**<br>• Open **Calendar** tab -> show 7-column month grid.<br>• Tap "Add to Calendar" (implicit `CalendarContract` intent).<br>• Tap "7-Day Coach Plan".<br>• Open **Leaderboard** tab -> toggle Today / 7d / 30d / Custom Date Range. | • Point out multi-platform aggregation (CF, AtCoder, LeetCode, CodeChef, CLIST).<br>• Show `MaterialDatePicker` range selection.<br>• Explain Firestore security rules and client-side window aggregation. |

---

## Part 2: 15 Viva & Technical Defense Questions

### Q1: Why did you use `AlarmManager` instead of `WorkManager`?
> **Answer:** `WorkManager` was explicitly forbidden by the course syllabus. Furthermore, `WorkManager` cannot guarantee exact millisecond execution because it batches background jobs based on battery and system constraints. `AlarmManager.setExactAndAllowWhileIdle()` guarantees exact execution even when the device is in Doze mode, which is critical for contest start alerts and spaced repetition upsolve reminders.

### Q2: How does `VirtualRunActivity` survive process death and configuration changes without losing the timer state?
> **Answer:** We never store remaining time in an in-memory countdown counter. Instead, when a run begins, we record the absolute epoch start time (`startTimeMs`) and contest duration in Room and `SharedPreferences`. In `onSaveInstanceState()` and `onResume()`, the remaining time is always computed as:
> $$\text{remainingMs} = \text{durationMs} - (\text{System.currentTimeMillis()} - \text{startTimeMs})$$
> Even if Android kills the process to reclaim memory, the timer resumes with exact precision upon reopening.

### Q3: How do you prevent Codeforces from returning HTTP 429 (Too Many Requests)?
> **Answer:** All Codeforces API calls are funneled through a singleton `RateLimiter` class located in `com.kuet.codearena.data.remote.RateLimiter`. It enforces a synchronized minimum interval of 2000 ms (2.0 seconds) between consecutive requests using `SystemClock.elapsedRealtime()`, queuing caller threads if needed.

### Q4: How is the custom `CircularTimerView` drawn?
> **Answer:** `CircularTimerView` extends `android.view.View`. It extracts custom attributes from `res/values/attrs.xml` (ring colors, stroke width, text sizes). In `onDraw(Canvas canvas)`, it calculates the bounding rectangle of the view and calls `canvas.drawArc()` twice: once for the background track (360°) and once for the progress arc based on `(progress / maxProgress) * 360°`. It then draws centered multi-line text using `Paint.Align.CENTER`.

### Q5: How do alarms survive when the user restarts their phone?
> **Answer:** Android clears all pending `AlarmManager` intents upon device shutdown. To handle this, we registered `BootReceiver` in `AndroidManifest.xml` listening for `android.intent.action.BOOT_COMPLETED`. When triggered, `BootReceiver` queries `AppDatabase` via Room on a background thread for all active future contest reminders and due upsolve items, and reschedules their exact alarms.

### Q6: What is `goAsync()` in `AlarmReceiver` and why is it necessary?
> **Answer:** Standard BroadcastReceivers run on the main UI thread with an ANR (Application Not Responding) timeout of approximately 10 seconds. For `ACTION_DAILY_SYNC`, we need to query remote APIs and update Room. Calling `goAsync()` signals to the Android OS that the receiver will continue processing asynchronously. We dispatch the work to `AppExecutors.networkIO()` and call `pendingResult.finish()` once all platforms have synchronized.

### Q7: How does your `ContentProvider` protect user data?
> **Answer:** `UpsolveContentProvider` implements read-only query operations. Any calls to `insert()`, `update()`, or `delete()` immediately throw an `UnsupportedOperationException`. Furthermore, it exposes only non-sensitive problem metadata (`problem_name`, `contest_id`, `rating`, `due_date`), preventing third-party apps from modifying or tampering with contest records.

### Q8: What happens if the student runs the app in an area with no internet connection?
> **Answer:** CodeArena is designed strictly offline-first. All past contests, problems, upsolve items, and daily stats are cached in Room SQLite. When offline:
> * Contests and problems load instantly from local SQLite.
> * The AI Coach falls back to the local `CoachEngine` rule engine.
> * Tag tips are parsed locally from `res/raw/tag_tips.xml` via `XmlPullParser`.
> * Leaderboards show local competitive peer benchmarks.

### Q9: How is the Practice Rating calculated?
> **Answer:** We use an ELO rating formula adapted for single-problem solves. Starting from an 800 baseline, when problem $p$ is solved, the expected probability of solving it is:
> $$E = \frac{1}{1 + 10^{\frac{p - R}{400}}}$$
> The rating increases by:
> $$\Delta R = 20 \times (1 - E)$$
> Expired upsolve items incur a 5-point penalty, ensuring students stay motivated to upsolve regularly.

### Q10: Why did you use `XmlPullParser` instead of DOM or SAX for `tag_tips.xml`?
> **Answer:** `XmlPullParser` is Android's built-in, forward-only streaming XML parser. Unlike DOM (which builds a memory-intensive tree of the entire document), `XmlPullParser` uses minimal memory and is significantly faster on mobile devices.

### Q11: Explain your implicit Intent usage.
> **Answer:** We use implicit intents for three core scenarios:
> 1. **View Problem in Browser:** `new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))` lets the user view the problem on Codeforces/AtCoder in their preferred browser.
> 2. **Share Contest Result:** `new Intent(Intent.ACTION_SEND)` with `text/plain` MIME type allows sharing score summaries across WhatsApp, Telegram, or email.
> 3. **Add to Phone Calendar:** `new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)` inserts the contest into Google Calendar without requiring invasive runtime calendar write permissions.

### Q12: How are LeetCode and AtCoder difficulties normalized?
> **Answer:** AtCoder Problems API reports direct point values ($P$), which we map via $\text{rating} \approx \max(800, P \times 3)$. LeetCode GraphQL problems report categories ("Easy", "Medium", "Hard"), which we map to canonical ratings: Easy $\approx 1000$, Medium $\approx 1500$, and Hard $\approx 2000$.

### Q13: How does the AI Coach preserve user privacy?
> **Answer:** `GeminiAiCoachClient` sends **zero Personally Identifiable Information (PII)**. Neither usernames, emails, nor platform handles are sent to Google Gemini. The payload contains only aggregated, anonymous numbers: current practice rating, active streak length, solved count today, pending upsolve count, and the top 3 weak category tags.

### Q14: What is the Firestore data model and security structure?
> **Answer:** Competitor profiles are stored at `users/{uid}` with fields `displayName`, `photoUrl`, `handles`, `cfRating`, `practiceRating`, `updatedAt`, and a `daily` map of `yyyyMMdd -> {solved, points}`. The Firestore security rules allow public reads for all authenticated users to power global leaderboards, while strictly restricting write operations to `request.auth.uid == userId`.

### Q15: How does the 5-step practice ladder choose problems?
> **Answer:** `CoachEngine.buildPracticeLadder()` rounds the user's current practice rating to the nearest 100 (level $L$). It queries cached Codeforces problems for the chosen weak tag at ratings $(L-100, L, L+100, L+200, L+300)$, sorting by highest `solvedCount` (classic benchmark problems) and strictly excluding any problem ID already present in the user's `solves` database.

---

## Part 3: Demonstration Fallback & Emergency Procedures

1. **If Codeforces API is undergoing scheduled maintenance:**
   * The app will gracefully display cached contests and problems from Room.
   * Inform the examiner: *"Codeforces is currently returning HTTP 503; the app's offline caching layer is serving the contest repository without disruption."*
2. **If Internet is unavailable on the presentation machine:**
   * Virtual Run mode, Spaced Repetition, Canvas Timers, XML Parsing, Practice Ladder, and Offline Leaderboard all execute 100% locally from Room SQLite.
3. **If Google Play Services is missing on an emulator:**
   * Tap *"Continue without account"* on the login screen to enter guest mode with all local and simulated leaderboard features enabled.
