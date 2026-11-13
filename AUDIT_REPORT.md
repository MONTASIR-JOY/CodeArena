# CodeArena Security and Data-Sync Audit

**Review date:** 2026-11-14  
**Review type:** Static source, manifest, Gradle, Firestore rules, and documentation review  
**Verification:** `:app:testDebugUnitTest`, `:app:assembleDebug`, and `:app:assembleRelease` succeeded. The debug app installed and launched on the emulator. UI hierarchy inspection timed out. No app test source files were present, so the unit-test task had no project tests to execute.  
**Scope limits:** The configured Firebase project and deployed rules were not inspected in this revision; live sync and rules deployment remain unverified. Provider API key restrictions and Play policy declarations were not reviewed.

## Summary

The high-priority issues from the prior audit have been addressed in the working tree: the app's ContentProvider and internal login/boot components are no longer exported, release and CI APKs omit Gemini/Clist API keys, Firestore read failures no longer trigger profile uploads, and Firestore rules validate owner identity plus the profile document schema.

The app syncs its user profile and selected preferences, not Room database records. Live cloud storage and deployed rules still need verification in the configured Firebase project.

## Remediated findings

### Exported provider exposed local records — fixed in source

The provider now declares `android:exported="false"` and `android:grantUriPermissions="false"`. LoginActivity and BootReceiver are also private app components. This keeps other apps from querying or mutating local Room records through the provider.

### Firestore read errors were treated as missing documents — fixed in source

`FirestoreSyncRepository` now uploads an initial profile only after a successful read confirms the document is absent. A failed read returns a failed callback and does not upload local state. Profile restoration uses one SharedPreferences edit, handles numeric or string read IDs, and does not trigger a burst of uploads while applying downloaded values.

### Firestore profile fields were incomplete — expanded in source

The profile document now stores and restores:

- Handles and solved counts for Codeforces, LeetCode, CodeChef, AtCoder, HackerRank, and CSES.
- Current ratings for Codeforces, LeetCode, CodeChef, and AtCoder.
- Peak ratings for LeetCode, CodeChef, and AtCoder.
- Friend handles, read blog IDs, and read catalog IDs.
- Daily target, notification and review settings, theme settings, and update time.

The document is written under `users/{uid}`. Email is not copied into the profile document. Local Room records, cached API data, and credentials are not uploaded.

### Firestore rules — tightened in source

[`firestore.rules`](firestore.rules) enforces owner-only access, an exact top-level schema, typed nested maps, bounded handle lengths, nonnegative metric counts, and list-size limits. These checked-in rules must still be published to the configured Firebase project; deployment was not verified.

## Remaining risks and limits

### Provider credentials require a backend for production use

Local debug builds can still read Gemini and Clist credentials from `local.properties`; these values are extractable from a debug APK and must not be shared. Release builds and the GitHub Actions debug artifact use empty credentials. Securely enabling these integrations in a distributed build requires a server-side proxy. Existing provider keys should be rotated if a keyed APK was distributed.

### Local Room records are not cloud-synchronized

Virtual runs, notes, problem records, and other Room data remain on-device. The Firestore profile sync does not back up the Room database.

### Feature gaps

Milestone alerts, upsolve reminders, timer notification actions, and live widget progress remain incomplete or unimplemented. README claims were narrowed to describe the current behavior.

### Room migrations

The destructive migration fallback was removed to prevent silent deletion. The database is currently at schema version 1; add explicit Room migrations before changing the schema.

### Automated and live verification

No app test source files were present during the audit. Firebase Console data and deployed security rules were not accessible for this review, so the reported Firestore schema and security behavior are based on source inspection only.

## Verification checklist

- [x] Gradle unit-test task, debug APK, and release APK completed successfully. No app test source files were present.
- [x] Android Studio deployed the debug app and reported success launching the default activity. A later UI hierarchy inspection timed out.
- [ ] Verify the new profile schema against the configured Firebase project and deploy the checked-in rules; live project access was not available.
- [ ] Publish the audited tree to the intended GitHub default branch.
