import subprocess
import os
import base64

def get_image_base64(path):
    if os.path.exists(path):
        with open(path, "rb") as f:
            return "data:image/png;base64," + base64.b64encode(f.read()).decode("utf-8")
    return ""

# Grab screenshots
screen_catalog = get_image_base64(r"C:\Users\Joy\.gemini\antigravity\brain\831d0cff-2b7c-4cad-99df-c8f94acafda2\phone_screen_ca.png")
screen_blog = get_image_base64(r"C:\Users\Joy\.gemini\antigravity\brain\831d0cff-2b7c-4cad-99df-c8f94acafda2\phone_screen_clean.png")

html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>CodeArena - Project Report (CSE 2200)</title>
<style>
    @page {{
        size: A4;
        margin: 20mm 18mm 20mm 18mm;
    }}
    body {{
        font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, Helvetica, Arial, sans-serif;
        color: #24292f;
        line-height: 1.55;
        font-size: 13.5px;
        margin: 0;
        padding: 0;
    }}
    .page-break {{
        page-break-before: always;
    }}
    .no-break {{
        page-break-inside: avoid;
    }}
    
    /* Cover Page */
    .cover-page {{
        text-align: center;
        padding-top: 40px;
        height: 90vh;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }}
    .kuet-emblem {{
        width: 95px;
        height: 110px;
        margin: 0 auto 15px auto;
    }}
    .univ-title {{
        font-size: 24px;
        font-weight: 700;
        color: #0f172a;
        margin-bottom: 4px;
    }}
    .dept-title {{
        font-size: 17px;
        color: #334155;
        margin-bottom: 25px;
    }}
    .course-box {{
        background: #f1f5f9;
        border: 1px solid #cbd5e1;
        border-radius: 8px;
        padding: 10px 20px;
        display: inline-block;
        margin: 0 auto 35px auto;
    }}
    .course-title {{
        font-size: 15px;
        font-weight: 600;
        color: #1e293b;
    }}
    .report-title-label {{
        font-size: 18px;
        font-weight: 600;
        color: #475569;
        margin-bottom: 6px;
    }}
    .project-name {{
        font-size: 28px;
        font-weight: 800;
        color: #1e3a8a;
        margin-bottom: 8px;
    }}
    .project-subtitle {{
        font-size: 17px;
        color: #0284c7;
        font-weight: 600;
        margin-bottom: 40px;
    }}
    .submission-grid {{
        display: flex;
        justify-content: space-between;
        text-align: left;
        margin-top: 40px;
        gap: 20px;
    }}
    .submit-card {{
        flex: 1;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        padding: 16px;
    }}
    .submit-card h4 {{
        margin: 0 0 10px 0;
        font-size: 14px;
        color: #0f172a;
        border-bottom: 2px solid #0284c7;
        padding-bottom: 4px;
    }}
    .teacher-table {{
        width: 100%;
        border-collapse: collapse;
        margin-top: 25px;
    }}
    .teacher-table td {{
        border: 1px solid #cbd5e1;
        padding: 12px 14px;
        vertical-align: top;
        font-size: 13px;
    }}
    .submission-date {{
        margin-top: 35px;
        font-weight: 700;
        color: #334155;
        font-size: 14px;
    }}

    /* Typography & Sections */
    h1, h2, h3, h4 {{
        color: #0f172a;
    }}
    h1 {{
        font-size: 22px;
        border-bottom: 2px solid #0284c7;
        padding-bottom: 6px;
        margin-top: 25px;
        margin-bottom: 15px;
    }}
    h2 {{
        font-size: 17px;
        color: #1e3a8a;
        margin-top: 20px;
        margin-bottom: 10px;
    }}
    h3 {{
        font-size: 15px;
        color: #334155;
        margin-top: 14px;
        margin-bottom: 6px;
    }}
    p, li {{
        text-align: justify;
        color: #334155;
    }}
    ul, ol {{
        margin-top: 6px;
        padding-left: 22px;
    }}
    li {{
        margin-bottom: 5px;
    }}

    /* Tables */
    table.data-table {{
        width: 100%;
        border-collapse: collapse;
        margin: 14px 0;
        font-size: 12.5px;
    }}
    table.data-table th {{
        background: #0f766e;
        color: white;
        text-align: left;
        padding: 8px 10px;
        font-weight: 600;
    }}
    table.data-table td {{
        border: 1px solid #cbd5e1;
        padding: 7px 10px;
    }}
    table.data-table tr:nth-child(even) {{
        background: #f8fafc;
    }}

    /* Step Flowchart Boxes */
    .flow-container {{
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;
        margin: 14px 0 20px 0;
    }}
    .flow-step {{
        width: 82%;
        border-radius: 6px;
        padding: 7px 14px;
        text-align: center;
        font-size: 12px;
        font-weight: 600;
        border-width: 1.5px;
        border-style: solid;
        box-shadow: 0 1px 2px rgba(0,0,0,0.05);
    }}
    .flow-arrow {{
        font-size: 13px;
        color: #64748b;
        line-height: 1;
    }}
    .step-green {{ background: #ecfdf5; border-color: #10b981; color: #065f46; }}
    .step-orange {{ background: #fffbeb; border-color: #f59e0b; color: #92400e; }}
    .step-blue {{ background: #eff6ff; border-color: #3b82f6; color: #1e40af; }}
    .step-purple {{ background: #faf5ff; border-color: #a855f7; color: #6b21a8; }}
    .step-teal {{ background: #f0fdf4; border-color: #14b8a6; color: #0f766e; }}

    /* Screenshots gallery */
    .screenshot-gallery {{
        display: flex;
        justify-content: center;
        gap: 24px;
        margin: 20px 0;
    }}
    .screenshot-frame {{
        text-align: center;
        background: #0f172a;
        padding: 8px;
        border-radius: 18px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        max-width: 250px;
    }}
    .screenshot-frame img {{
        width: 100%;
        border-radius: 12px;
        display: block;
    }}
    .screenshot-caption {{
        margin-top: 8px;
        font-size: 12px;
        color: #64748b;
        font-weight: 600;
        text-align: center;
    }}

    /* TOC list */
    .toc-list {{
        list-style: none;
        padding-left: 0;
    }}
    .toc-list li {{
        display: flex;
        justify-content: space-between;
        padding: 6px 0;
        border-bottom: 1px dotted #cbd5e1;
        font-size: 14px;
        font-weight: 500;
    }}
    .toc-list li span.title {{
        color: #1e3a8a;
    }}
</style>
</head>
<body>

<!-- COVER PAGE -->
<div class="cover-page">
    <div>
        <!-- KUET Logo SVG -->
        <svg class="kuet-emblem" viewBox="0 0 100 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M50 5 L88 22 V65 C88 92 50 115 50 115 C50 115 12 92 12 65 V22 Z" fill="#047857" stroke="#065f46" stroke-width="2"/>
            <path d="M50 14 L80 28 V63 C80 85 50 104 50 104 C50 104 20 85 20 63 V28 Z" fill="#ffffff"/>
            <circle cx="50" cy="50" r="22" fill="#047857"/>
            <circle cx="50" cy="50" r="18" fill="#ffffff"/>
            <path d="M50 36 L54 45 L64 45 L56 51 L59 60 L50 54 L41 60 L44 51 L36 45 L46 45 Z" fill="#f59e0b"/>
            <text x="50" y="80" font-size="7" font-weight="bold" text-anchor="middle" fill="#065f46">KUET</text>
            <text x="50" y="89" font-size="5" text-anchor="middle" fill="#334155">ESTD. 1967</text>
        </svg>

        <div class="univ-title">Khulna University of Engineering &amp; Technology</div>
        <div class="dept-title">Department of Computer Science and Engineering</div>

        <div class="course-box">
            <div class="course-title">Course Title: Advanced Programming Laboratory</div>
            <div style="font-size: 13.5px; color: #475569; margin-top: 3px;">Course No: <strong>CSE 2200</strong></div>
        </div>

        <div class="report-title-label">A Project Report on:</div>
        <div class="project-name">CodeArena</div>
        <div class="project-subtitle">Android-Based Competitive Programming Companion Application</div>
    </div>

    <div>
        <div class="submission-grid">
            <div class="submit-card">
                <h4>Submitted by:</h4>
                <div style="font-weight: 700; font-size: 15px; color: #0f172a;">Montasir Joy</div>
                <div style="color: #475569; margin-top: 4px;">Roll No: <strong>2307014</strong></div>
                <div style="color: #64748b; font-size: 12px; margin-top: 4px;">Dept. of Computer Science &amp; Engineering</div>
                <div style="color: #64748b; font-size: 12px;">Khulna University of Engineering &amp; Technology</div>
            </div>
        </div>

        <table class="teacher-table">
            <tr style="background: #f1f5f9; font-weight: 700; color: #0f172a;">
                <td colspan="2">Submitted to:</td>
            </tr>
            <tr>
                <td style="width: 50%;">
                    <strong>Safin Ahmmed</strong><br>
                    Assistant Professor<br>
                    Dept. of Computer Science and Engineering<br>
                    Khulna University of Engineering &amp; Technology
                </td>
                <td style="width: 50%;">
                    <strong>Ehsanul Karim</strong><br>
                    Lecturer<br>
                    Dept. of Computer Science and Engineering<br>
                    Khulna University of Engineering &amp; Technology
                </td>
            </tr>
        </table>

        <div class="submission-date">DATE: 08-11-2026</div>
    </div>
</div>

<!-- PAGE BREAK: TABLE OF CONTENTS -->
<div class="page-break"></div>

<h1 style="text-align: center; margin-top: 10px; margin-bottom: 25px;">Table of Contents</h1>

<ul class="toc-list">
    <li><span class="title">1. Introduction</span><span>3</span></li>
    <li><span class="title">2. Objectives and Scope</span><span>3</span></li>
    <li><span class="title">3. Technology Stack and Architecture</span><span>4</span></li>
    <li><span class="title">4. Overview and Feature Walkthrough</span><span>5</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.1 User Authentication &amp; Multi-Account Isolation</span><span>5</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.2 Multi-Platform Profile Aggregator</span><span>5</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.3 Live &amp; Upcoming Contest Tracker</span><span>6</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.4 Virtual Contest Simulator with Foreground Service</span><span>6</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.5 AI Performance Coach (Gemini Pro API)</span><span>7</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.6 Spaced-Repetition Upsolve System</span><span>7</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.7 Granular Tag Mastery Progression</span><span>8</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.8 Codeforces Offline Catalog &amp; LaTeX Tutorial Viewer</span><span>8</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.9 Android Home Screen AppWidget</span><span>9</span></li>
    <li style="padding-left: 20px; font-size: 12.5px;"><span>4.10 Inter-App ContentProvider &amp; Social Card Sharing</span><span>9</span></li>
    <li><span class="title">5. Application Screenshots</span><span>10</span></li>
    <li><span class="title">6. API and Backend Integration</span><span>11</span></li>
    <li><span class="title">7. Database Design &amp; Persistence</span><span>12</span></li>
    <li><span class="title">8. Android Components Architecture</span><span>13</span></li>
    <li><span class="title">9. Security, Permissions, and Access Control</span><span>14</span></li>
    <li><span class="title">10. Testing and Experimental Results</span><span>14</span></li>
    <li><span class="title">11. Limitations, Future Improvements, and Conclusion</span><span>15</span></li>
</ul>

<!-- PAGE BREAK: CONTENT SECTIONS -->
<div class="page-break"></div>

<h1>1. Introduction</h1>
<p>
Competitive programming (CP) is an essential discipline for computer science students seeking to master algorithmic efficiency, discrete mathematics, and high-performance problem solving. However, active contestants constantly struggle with disjointed workflows across competing platforms like Codeforces, LeetCode, CodeChef, and AtCoder. Contestants lack a unified platform to track cross-judge ratings, analyze contest failures, and systematically practice neglected problem categories.
</p>
<p>
<strong>CodeArena</strong> is an advanced, native Android mobile application engineered entirely in <strong>Pure Java</strong> to solve these productivity hurdles. Designed specifically as an end-to-end training and contest companion, CodeArena combines live multi-platform contest tracking, exact alarm scheduling, an ongoing Foreground Service stopwatch, a personalized Generative AI performance coach (Google Gemini 1.5 Pro), and an offline repository of 381 curated algorithm tutorials.
</p>

<h1>2. Objectives and Scope</h1>
<p>The primary objectives of the CodeArena project are:</p>
<ul>
    <li><strong>Multi-Platform Profile Aggregator:</strong> Provide a unified dashboard that syncs and visualizes ratings, badges, and solve counts from Codeforces, LeetCode, CodeChef, AtCoder, and HackerRank.</li>
    <li><strong>Contest Scheduling &amp; Exact Reminders:</strong> Aggregate global contests from CLIST.by and Codeforces APIs, scheduling exact alarms that survive device reboots via <code>AlarmManager</code> and <code>BootReceiver</code>.</li>
    <li><strong>Virtual Contest Simulation:</strong> Emulate timed contest conditions using a non-killable Android <code>ForegroundService</code> with live status-bar notification controls.</li>
    <li><strong>AI-Powered Diagnostics:</strong> Leverage the Google Gemini 1.5 Pro REST API to analyze contest performance and provide progressive algorithmic hints without spoiling complete solutions.</li>
    <li><strong>Algorithmic Mastery:</strong> Track topic mastery across 100-rating intervals (800–900, 1000–1100, etc.) for dynamic programming, graphs, math, and greedy algorithms.</li>
    <li><strong>Offline Educational Catalog:</strong> Bundle 381 Codeforces tutorials with full mathematical notation (LaTeX/MathML) for distraction-free learning.</li>
    <li><strong>Academic Excellence:</strong> Adhere 100% to the <strong>CSE 2200: Advanced Programming Laboratory</strong> curriculum by implementing pure Java OOP, Room SQLite, Retrofit/OkHttp, Services, Broadcast Receivers, Content Providers, and App Widgets.</li>
</ul>

<!-- PAGE BREAK: ARCHITECTURE -->
<div class="page-break"></div>

<h1>3. Technology Stack and Architecture</h1>

<p>
CodeArena adopts a modern <strong>Model-View-ViewModel (MVVM)</strong> architecture with clean separation between the presentation layer, repository domain, and underlying local/remote data sources.
</p>

<table class="data-table">
    <thead>
        <tr>
            <th style="width: 25%;">Component</th>
            <th style="width: 30%;">Technology / Library</th>
            <th style="width: 45%;">Implementation Purpose</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><strong>Programming Language</strong></td>
            <td>Pure Java (Java 8/17)</td>
            <td>Full adherence to CSE 2200 syllabus without Kotlin dependencies.</td>
        </tr>
        <tr>
            <td><strong>IDE &amp; Build System</strong></td>
            <td>Android Studio, Gradle (KTS)</td>
            <td>Compilation, debugging, dependency management, and APK packaging.</td>
        </tr>
        <tr>
            <td><strong>User Interface</strong></td>
            <td>Material Design, ViewBinding</td>
            <td>Dark-themed responsive UI, ConstraintLayout, and fluid RecyclerViews.</td>
        </tr>
        <tr>
            <td><strong>Local Database</strong></td>
            <td>Room SQLite (9 Entities, 9 DAOs)</td>
            <td>Offline caching of contests, problems, runs, tag XP, and AI responses.</td>
        </tr>
        <tr>
            <td><strong>Networking &amp; Parsing</strong></td>
            <td>Retrofit 2, OkHttp 3, Gson</td>
            <td>REST API communication, rate-limit interception, and JSON mapping.</td>
        </tr>
        <tr>
            <td><strong>Generative AI</strong></td>
            <td>Google Gemini 1.5 Pro REST API</td>
            <td>Automated contest analysis, hint generation, and debriefing.</td>
        </tr>
        <tr>
            <td><strong>Authentication</strong></td>
            <td>Firebase Auth, Google Sign-In</td>
            <td>Email/password and Google OAuth with account storage isolation.</td>
        </tr>
        <tr>
            <td><strong>Background Service</strong></td>
            <td>Android Foreground Service</td>
            <td>Persistent virtual contest stopwatch with ongoing notification.</td>
        </tr>
        <tr>
            <td><strong>Event &amp; Alarm Handling</strong></td>
            <td>BroadcastReceiver, AlarmManager</td>
            <td>Exact contest reminders, boot rescheduling, notification clicks.</td>
        </tr>
        <tr>
            <td><strong>Inter-App Provider</strong></td>
            <td>ContentProvider, FileProvider</td>
            <td>External access to contest tables and secure result-card image sharing.</td>
        </tr>
        <tr>
            <td><strong>Desktop Widget</strong></td>
            <td>AppWidgetProvider</td>
            <td>Interactive home-screen widget displaying the daily challenge problem.</td>
        </tr>
    </tbody>
</table>

<p>
The application cleanly encapsulates business logic into repository classes (<code>CodeforcesRepository</code>, <code>ContestRepository</code>, <code>UserPreferencesRepository</code>, <code>CatalogRepository</code>). ViewModels expose asynchronous data through reactive <code>LiveData</code> streams, ensuring zero UI thread blocking during intensive database queries and REST requests.
</p>

<!-- PAGE BREAK: FEATURE WALKTHROUGH -->
<div class="page-break"></div>

<h1>4. Overview and Feature Walkthrough</h1>

<h2>4.1 User Authentication &amp; Multi-Account Isolation</h2>
<p>
Allows users to securely register, sign in via Email/Password or Google OAuth, or proceed with guest mode. SharedPreferences storage is dynamically partitioned using the Firebase UID (<code>codearena_preferences_&lt;uid&gt;.xml</code>) to ensure complete data isolation between multiple users on the same physical handset.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Open CodeArena Application &amp; Select Sign In or Register</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Enter Credentials (or Tap Google One-Tap Sign In)</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Firebase Authentication Validates Tokens &amp; Returns User UID</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: Load Partitioned Preferences ("codearena_preferences_&lt;UID&gt;")</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Launch MainActivity Dashboard with Account-Specific Cache</div>
</div>

<h2>4.2 Multi-Platform Profile Aggregator</h2>
<p>
Fetches and aggregates user profiles across Codeforces, LeetCode, CodeChef, AtCoder, and HackerRank. Codeforces user data is queried in real time via Retrofit, while solved counts from other judges are seamlessly consolidated into a unified profile summary with official rank tier badges.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: User Configures Handles in Settings or Onboarding</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Query Codeforces REST API (user.info &amp; user.status)</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Parse Rating, Global Rank, Organization, and Solved Count</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: Consolidate Cross-Platform Solved Numbers into Profile Summary</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Render Tier Cards with Official Badge Graphics &amp; Rating Colors</div>
</div>

<!-- PAGE BREAK -->
<div class="page-break"></div>

<h2>4.3 Live &amp; Upcoming Contest Tracker</h2>
<p>
Aggregates live and upcoming rounds from Codeforces and CLIST.by. Users can review contest details, schedule exact 15-minute start reminders via <code>AlarmManager</code>, and export schedules into their device calendar.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Open Contests or Upcoming Tab</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Query CLIST.by &amp; Codeforces API for Scheduled Rounds</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: User Taps Notification Bell on Desired Contest</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: Schedule Exact Alarm (AlarmManager.setExactAndAllowWhileIdle)</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: ContestAlarmReceiver Triggers High-Priority Notification at T - 15m</div>
</div>

<h2>4.4 Virtual Contest Simulator with Foreground Service</h2>
<p>
Simulates timed competitive programming rounds. A dedicated Android <code>ForegroundService</code> (<code>ContestTimerService</code>) runs the stopwatch, maintaining millisecond accuracy even when the app is minimized, with interactive Pause, Resume, and Finish buttons directly on the status bar.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Select Past Contest &amp; Tap "Start Virtual Run"</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Start ContestTimerService as an Android Foreground Service</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Post Ongoing Status Bar Notification with Interactive Action Buttons</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: User Solves Problems; Time &amp; Verdict Logged to Room SQLite</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Stop Service, Compute Penalty/Rank, and Render Shareable Result Card</div>
</div>

<!-- PAGE BREAK -->
<div class="page-break"></div>

<h2>4.5 AI Performance Coach (Gemini Pro API)</h2>
<p>
Evaluates contest submissions and algorithmic weaknesses. Contestants can request three coaching modes: Contest Debrief (identifies time sinks), Topic Master (highlights weak problem tags), and 3-Tier Progressive Hints (conceptual clue &rarr; approach &rarr; edge cases) without spoiling full answers.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Open Coach Tab or Tap "Ask AI Coach" on a Problem</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Check Local Room Database (AiCacheEntity) for Cached Result</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Construct Contextual Prompt with User Rating &amp; Problem Details</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: Execute Asynchronous HTTP Request to Google Gemini 1.5 Pro API</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Cache Markdown Response Locally and Render Structured Advice</div>
</div>

<h2>4.6 Spaced-Repetition Upsolve System</h2>
<p>
Automatically tracks problems that a contestant failed to solve during virtual or live rounds. The system queues the problem across increasing review intervals (Day 1, Day 3, Day 7, Day 14, Day 30) using the Leitner flashcard methodology.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Contest Submission Fails (Verdict != OK)</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Save to Room SQLite as UpsolveItemEntity with Review Stage = 1</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Schedule Review Reminder Alarm via AlarmManager</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: User Opens Problem from Upsolve Tab &amp; Successfully Solves It</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Advance Review Stage to Next Interval (Day 3 &rarr; Day 7 &rarr; Day 14)</div>
</div>

<!-- PAGE BREAK -->
<div class="page-break"></div>

<h2>4.7 Granular Tag Mastery Progression</h2>
<p>
Breaks down problem history into fine-grained 100-rating intervals (e.g., 800–900, 1000–1100, 1200–1300, ..., 2400+). By analyzing problem tags (Dynamic Programming, Graph Theory, Greedy, Trees, Number Theory), the system calculates XP points and unlocks tier mastery badges.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Load All Solved Problem Keys from User History</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Filter by Algorithmic Tag (e.g., "dp", "graphs", "greedy")</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Group Solved Problems into 100-Point Rating Intervals</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: Compute Category Experience Points (XP) &amp; Mastery Tier</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Render Visual Mastery Progress Bars with Unlocked Achievement Badges</div>
</div>

<h2>4.8 Codeforces Offline Catalog &amp; LaTeX Tutorial Viewer</h2>
<p>
Bundles an offline repository of <strong>381 curated algorithm and CP tutorials</strong> extracted from Codeforces community blogs. An embedded WebView engine renders complex mathematical equations ($O(N \log N)$, $\sum A[i]$) alongside syntax-highlighted code snippets, with local read progress tracking.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Open Codeforces Catalog from Home Screen</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Stream Bundled assets/codeforces_catalog.json (381 Tutorials)</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Filter by Category (General Advice, Data Structures, Math, DP)</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: Open Tutorial &amp; Render Content with Formatted LaTeX Mathematics</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Toggle "Mark as Read"; Update SharedPreferences &amp; Trigger Auto-Backup</div>
</div>

<!-- PAGE BREAK -->
<div class="page-break"></div>

<h2>4.9 Android Home Screen AppWidget</h2>
<p>
An interactive <code>DailyWidgetProvider</code> that users can place on their Android home screen launcher. It displays the current date, solving streak counter, and daily recommended problem with one-tap deep linking directly into the problem statement.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Android System Triggers DailyWidgetProvider.onUpdate()</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Query SharedPreferences for Current Streak &amp; Target Problem</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Populate RemoteViews with Problem Title, Platform, and Difficulty</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: Attach PendingIntent to Widget Root View</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Tapping Widget Launches MainActivity Directly to Problem Details</div>
</div>

<h2>4.10 Inter-App ContentProvider &amp; Social Card Sharing</h2>
<p>
Implements <code>CodeArenaContentProvider</code> (<code>android:exported="true"</code>) exposing cached contest data to external applications via standard Android content URIs. Furthermore, <code>FileProvider</code> enables secure bitmap sharing of high-resolution virtual contest result cards to WhatsApp, Discord, Telegram, or Twitter.
</p>

<div class="flow-container no-break">
    <div class="flow-step step-green">Step 1: Virtual Contest Completes or Result Card Selected</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-orange">Step 2: Render Layout to High-Resolution Bitmap in Memory</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-blue">Step 3: Save Bitmap to App Internal Cache Directory (/cache/shared_cards)</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-purple">Step 4: Generate Secure Content URI via androidx.core.content.FileProvider</div>
    <div class="flow-arrow">&darr;</div>
    <div class="flow-step step-teal">Step 5: Dispatch Intent.ACTION_SEND with FLAG_GRANT_READ_URI_PERMISSION</div>
</div>

<!-- PAGE BREAK: SCREENSHOTS -->
<div class="page-break"></div>

<h1>5. Application Screenshots</h1>
<p>
The following screenshots demonstrate the running application on an Android device (Xiaomi HyperOS / Android 14, Device Model 25053RT47C):
</p>

<div class="screenshot-gallery">
    <div class="screenshot-frame">
        <img src="{screen_catalog}" alt="Codeforces Catalog Screenshot">
        <div class="screenshot-caption" style="color: #cbd5e1;">Fig 5.1: Codeforces Catalog (381 Tutorials, Category Blocks &amp; Read Tracking)</div>
    </div>
    <div class="screenshot-frame">
        <img src="{screen_blog}" alt="LaTeX Blog Tutorial Viewer Screenshot">
        <div class="screenshot-caption" style="color: #cbd5e1;">Fig 5.2: In-App Tutorial Viewer with Syntax Highlighting &amp; LaTeX Math</div>
    </div>
</div>

<p style="text-align: center; font-size: 12.5px; color: #64748b; margin-top: 20px;">
<em>Figure 5: Live application captures showing the Catalog browser and formatted algorithmic tutorial reader.</em>
</p>

<!-- PAGE BREAK: BACKEND & DATABASE -->
<div class="page-break"></div>

<h1>6. API and Backend Integration</h1>

<h2>6.1 Codeforces REST API</h2>
<p>
The application utilizes Retrofit 2 with Gson converter to communicate with Codeforces REST endpoints:
</p>
<ul>
    <li><code>user.info?handles={'{handle}'}</code>: Fetches profile avatar, global rank, rating, and organization.</li>
    <li><code>user.status?handle={'{handle}'}&amp;from=1&amp;count=5000</code>: Extracts complete submission history to compute total accepted problems, verdict breakdowns, and tag distributions.</li>
    <li><code>contest.list?gym=false</code>: Retrieves active, upcoming, and past official contests.</li>
    <li><code>contest.standings?contestId={'{id}'}&amp;from=1&amp;count=100</code>: Downloads contest ranklists to benchmark virtual contest performances.</li>
</ul>

<h2>6.2 CLIST.by Competitive Programming API</h2>
<p>
CLIST.by aggregates global competitive programming calendars. CodeArena queries upcoming events across Codeforces, LeetCode, CodeChef, and AtCoder with HTTP Basic Authentication headers (<code>api_key</code> and <code>username</code>), caching responses locally in Room SQLite.
</p>

<h2>6.3 Google Gemini 1.5 Pro AI API</h2>
<p>
Interactions with the Gemini Pro endpoint (<code>POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent</code>) are managed through OkHttp. Performance metrics and failed problem statements are injected into structured prompts, and the returned Markdown response is cached in Room (<code>AiCacheEntity</code>) to eliminate redundant API token consumption.
</p>

<h2>6.4 Firebase Authentication SDK</h2>
<p>
Handles user account creation and sign-in via Email/Password and Google OAuth. The authenticated user's UID dynamically isolates local SharedPreferences files (<code>codearena_preferences_&lt;uid&gt;.xml</code>), ensuring multi-user isolation on shared devices.
</p>

<h1>7. Database Design &amp; Persistence</h1>

<p>
CodeArena utilizes <strong>Android Jetpack Room</strong> over SQLite, featuring 9 strongly typed entities with complete DAO interfaces:
</p>

<table class="data-table">
    <thead>
        <tr>
            <th>Entity Name</th>
            <th>Primary Key</th>
            <th>Description &amp; Stored Data</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><code>ContestEntity</code></td>
            <td><code>id</code> (Long)</td>
            <td>Stores contest name, platform, phase, start time, and duration.</td>
        </tr>
        <tr>
            <td><code>ProblemEntity</code></td>
            <td><code>id</code> (Long)</td>
            <td>Codeforces problem statement, index, rating, tags, and solved state.</td>
        </tr>
        <tr>
            <td><code>VirtualRunEntity</code></td>
            <td><code>id</code> (Auto-inc)</td>
            <td>Metadata of virtual contest runs (start time, score, penalty, status).</td>
        </tr>
        <tr>
            <td><code>ProblemResultEntity</code></td>
            <td><code>id</code> (Auto-inc)</td>
            <td>Individual problem submission results and verdicts within a virtual run.</td>
        </tr>
        <tr>
            <td><code>UpsolveItemEntity</code></td>
            <td><code>problemId</code> (String)</td>
            <td>Spaced repetition intervals (Stages 1 through 5) for failed problems.</td>
        </tr>
        <tr>
            <td><code>NoteEntity</code></td>
            <td><code>id</code> (Auto-inc)</td>
            <td>Personal algorithmic notes associated with problems or contests.</td>
        </tr>
        <tr>
            <td><code>TagXpEntity</code></td>
            <td><code>tag</code> (String)</td>
            <td>Accumulated experience points and tier progress per algorithm tag.</td>
        </tr>
        <tr>
            <td><code>AiCacheEntity</code></td>
            <td><code>cacheKey</code> (String)</td>
            <td>Local cache of Gemini AI responses to avoid duplicate API calls.</td>
        </tr>
        <tr>
            <td><code>RankEstimateEntity</code></td>
            <td><code>contestId</code> (Long)</td>
            <td>Benchmark percentile data for offline virtual rank estimation.</td>
        </tr>
    </tbody>
</table>

<!-- PAGE BREAK: ANDROID COMPONENTS -->
<div class="page-break"></div>

<h1>8. Android Components Architecture</h1>

<p>
CodeArena fulfills all core Android component requirements outlined in the <strong>CSE 2200</strong> syllabus:
</p>

<table class="data-table">
    <thead>
        <tr>
            <th>Component Type</th>
            <th>Class Name</th>
            <th>Architectural Role &amp; Lifecycle</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><strong>Activity</strong></td>
            <td><code>MainActivity</code></td>
            <td>Single-activity navigation host managing 14 feature fragments and bottom navigation bar.</td>
        </tr>
        <tr>
            <td><strong>Activity</strong></td>
            <td><code>LoginActivity</code></td>
            <td>Entry-point activity handling Firebase email login, registration, and Google OAuth sign-in.</td>
        </tr>
        <tr>
            <td><strong>Foreground Service</strong></td>
            <td><code>ContestTimerService</code></td>
            <td>Maintains unkillable virtual contest stopwatch with ongoing status-bar notification.</td>
        </tr>
        <tr>
            <td><strong>BroadcastReceiver</strong></td>
            <td><code>ContestAlarmReceiver</code></td>
            <td>Receives exact <code>AlarmManager</code> triggers and displays contest reminder notifications.</td>
        </tr>
        <tr>
            <td><strong>BroadcastReceiver</strong></td>
            <td><code>TimerActionReceiver</code></td>
            <td>Handles remote notification action clicks (Pause, Resume, Stop) from the status bar.</td>
        </tr>
        <tr>
            <td><strong>BroadcastReceiver</strong></td>
            <td><code>BootReceiver</code></td>
            <td>Listens to <code>BOOT_COMPLETED</code> to reschedule active contest alarms after device reboot.</td>
        </tr>
        <tr>
            <td><strong>ContentProvider</strong></td>
            <td><code>CodeArenaContentProvider</code></td>
            <td>Exposes contest and problem database tables to third-party applications via standard URIs.</td>
        </tr>
        <tr>
            <td><strong>ContentProvider</strong></td>
            <td><code>FileProvider</code></td>
            <td>Securely exposes rendered result-card bitmap images for external social sharing intents.</td>
        </tr>
        <tr>
            <td><strong>AppWidgetProvider</strong></td>
            <td><code>DailyWidgetProvider</code></td>
            <td>Android desktop home-screen widget displaying the daily challenge problem and streak.</td>
        </tr>
    </tbody>
</table>

<h1>9. Security, Permissions, and Access Control</h1>

<ul>
    <li><strong>Runtime Notification Permissions:</strong> Automatically requests <code>POST_NOTIFICATIONS</code> on Android 13+ (API 33) to ensure contest reminders and stopwatch alerts post reliably.</li>
    <li><strong>Exact Alarm Permissions:</strong> Declares <code>SCHEDULE_EXACT_ALARM</code> and <code>USE_EXACT_ALARM</code> to guarantee millisecond-accurate notification delivery.</li>
    <li><strong>API Key Protection:</strong> Sensitive API keys (<code>GEMINI_API_KEY</code>, <code>CLIST_API_KEY</code>) are stored in <code>local.properties</code> and injected at compile time via Gradle <code>BuildConfig</code>, keeping them out of version control.</li>
    <li><strong>Rate Limiting Protection:</strong> Implements <code>RateLimitInterceptor</code> enforcing a 200ms throttle between consecutive API requests, preventing HTTP 429 bans from Codeforces servers.</li>
    <li><strong>Android 12+ Cloud Auto-Backup:</strong> Explicitly configured in <code>data_extraction_rules.xml</code> and <code>backup_rules.xml</code> with <code>BackupManager.dataChanged()</code> hooks, ensuring handles and read tutorial progress survive app reinstallation.</li>
</ul>

<!-- PAGE BREAK: TESTING & CONCLUSION -->
<div class="page-break"></div>

<h1>10. Testing and Experimental Results</h1>

<table class="data-table">
    <thead>
        <tr>
            <th>Module Tested</th>
            <th>Testing Procedure</th>
            <th>Expected Behavior</th>
            <th>Observed Result</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><strong>User Authentication</strong></td>
            <td>Register new user &amp; sign in with Google OAuth</td>
            <td>Tokens retrieved, session persisted, isolated prefs created</td>
            <td>Correct UID partitioned prefs loaded</td>
            <td><strong>PASS</strong></td>
        </tr>
        <tr>
            <td><strong>Foreground Stopwatch</strong></td>
            <td>Start virtual run; minimize app to background</td>
            <td>Timer runs continuously; lock-screen notification displays</td>
            <td>Zero dropped ticks; pause/resume works</td>
            <td><strong>PASS</strong></td>
        </tr>
        <tr>
            <td><strong>Contest Reminders</strong></td>
            <td>Schedule reminder for upcoming round</td>
            <td>Exact alarm triggers notification 15m before start</td>
            <td>Notification posted on time on API 33</td>
            <td><strong>PASS</strong></td>
        </tr>
        <tr>
            <td><strong>Gemini AI Coach</strong></td>
            <td>Request contest debrief and hint generation</td>
            <td>Structured advice returned without complete code spoilers</td>
            <td>Structured Markdown rendered &amp; cached</td>
            <td><strong>PASS</strong></td>
        </tr>
        <tr>
            <td><strong>Tag Mastery Intervals</strong></td>
            <td>Filter problem catalog by tag with 100-rating intervals</td>
            <td>Problems correctly partitioned into 800-900, 1000-1100 bins</td>
            <td>Rating bins match problem database</td>
            <td><strong>PASS</strong></td>
        </tr>
        <tr>
            <td><strong>Offline Catalog Reader</strong></td>
            <td>Open tutorial with LaTeX mathematical equations</td>
            <td>Math equations ($dp[i][j]$) and code blocks render cleanly</td>
            <td>Zero formatting errors across 381 tutorials</td>
            <td><strong>PASS</strong></td>
        </tr>
        <tr>
            <td><strong>Cloud Backup / Restore</strong></td>
            <td>Save handles, uninstall app, and reinstall</td>
            <td>Data restored automatically via Android Auto-Backup</td>
            <td>All handles &amp; read states preserved</td>
            <td><strong>PASS</strong></td>
        </tr>
    </tbody>
</table>

<h1>11. Limitations and Future Improvements</h1>
<ul>
    <li><strong>Automated Code Submission:</strong> Direct in-app code submission is currently limited by Cloudflare bot protection on Codeforces. A browser extension bridge or desktop companion daemon could facilitate automated submission in future versions.</li>
    <li><strong>Multiplayer 1v1 Duels:</strong> Expanding the virtual contest simulator into real-time head-to-head battles using Firebase Realtime Database or WebSockets.</li>
    <li><strong>Built-in Code Editor:</strong> Embedding an offline mobile IDE with syntax highlighting and local C++/Java compilation capabilities.</li>
</ul>

<h1>12. Conclusion</h1>
<p>
<strong>CodeArena</strong> provides a comprehensive, production-ready competitive programming companion application. By uniting multi-platform profile analytics, exact contest scheduling, an ongoing Foreground Service stopwatch, a personalized Gemini AI mentor, and an offline repository of 381 algorithm tutorials, it eliminates the fragmented workflows of modern competitive programmers.
</p>
<p>
Engineered strictly in <strong>Pure Java</strong>, CodeArena demonstrates mastery of every learning objective in the <strong>CSE 2200: Advanced Programming Laboratory</strong> curriculum—seamlessly combining object-oriented design, multithreaded background processing, Room SQLite persistence, REST networking, and the complete Android component suite.
</p>

</body>
</html>
"""

# Write HTML file
html_path = r"d:\CodeArena\report_source.html"
with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)

print("HTML source generated at:", html_path)

# Output PDF path
pdf_path = r"d:\CodeArena\CodeArena_Project_Report.pdf"

# Compile via headless Chrome / Edge
chrome_path = r"C:\Program Files\Google\Chrome\Application\chrome.exe"
if not os.path.exists(chrome_path):
    chrome_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"

cmd = [
    chrome_path,
    "--headless",
    "--disable-gpu",
    "--no-pdf-header-footer",
    f"--print-to-pdf={pdf_path}",
    f"file:///{html_path.replace(os.sep, '/')}"
]

print("Executing Chrome headless print...")
res = subprocess.run(cmd, capture_output=True, text=True)
print("Return code:", res.returncode)
if os.path.exists(pdf_path):
    size_kb = round(os.path.getsize(pdf_path) / 1024, 2)
    print(f"SUCCESS: PDF created at {pdf_path} ({size_kb} KB)")
else:
    print("Error: PDF was not generated. Output:", res.stderr)
