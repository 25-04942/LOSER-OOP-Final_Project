# LOS:ER (Life Observer System: Endangered Reality)

Android app with a POU-style pet whose health follows your daily screen time.
Java holds the OOP logic; a small Kotlin module reads usage data from Android.

## Project layout

```
loser/
├─ src/loser/                          plain Java: runs and is tested on a PC
│  ├─ Main.java                        console menu for testing the logic
│  ├─ model/        Category, UsageRecord, DailyRecord
│  ├─ core/         UsageTracker, Settings, Pet, WeeklyReport, TimeText
│  ├─ state/        PetState (abstract) + Healthy/Tired/Sick/Devastated
│  ├─ notification/ Notifiable (interface), NotificationService, UsageMonitor (what to alert, once per day)
│  ├─ exception/    InvalidSettingsException, MissingPermissionException
│  ├─ source/       UsageSource (interface), SampleUsageSource (fake data)
│  ├─ app/          AppSession  (objects shared by all screens + day rollover + history)
│  ├─ storage/      RecordCodec (daily records <-> text, for saving on the device)
│  ├─ ui/           Presenters + models for every screen, SettingsForm (text shown on screens)
│  └─ test/         AllTests (174 self-checking tests, no JUnit)
├─ docs/            UML class diagram (uml-classes.png / .mmd) and TEST-CHECKLIST.md
└─ android/
   ├─ FONT-LICENSE-Nunito-OFL.txt
   └─ app/src/main/                    Android files, built from the Figma design
      ├─ AndroidManifest.xml
      ├─ java/loser/android/   LoserApp, BaseScreenActivity, Setup/Main/Usage/History/Settings/ConnectActivity,
      │                        AppStore (saves data), UsageRefresher, AndroidNotificationService,
      │                        UsageCheckWorker (background check)   -- Java
      │                        UsageReader, PermissionHelper, AppCategorizer  -- Kotlin (Android usage data only)
      └─ res/  layout/, values/ (colors, strings, themes), drawable/, drawable-nodpi/ (pet pictures), font/
```

## Features (from the proposal)

| Proposal item | Where it is |
|---|---|
| Phase 1 setup: permission, create the pet, limit, reset time | `SetupActivity` then `ConnectActivity` (first launch only) |
| Pet states Healthy / Tired / Sick / Devastated with **adjustable thresholds** | `Pet.setThresholds`, `Settings` (defaults 50 / 80 / 100 %), Settings screen "Condition changes at" |
| Daily limit (must be above 0) | `Settings.setDailyLimit` (1 minute to 24 hours) |
| **Daily reset at the user's reset time** | `Settings.dayOf`, `AppSession.rollOverIfNewDay`; usage is counted from the reset time to the next one |
| Notifications: near limit, over limit, long single-app session, pet state change | `UsageMonitor` (rules, each sent once per day) + `AndroidNotificationService` (shows them) |
| Checks while the app is closed | `UsageCheckWorker` (WorkManager, about every 15 minutes) |
| One `DailyRecord` per day, saved on the device only | `AppSession` + `AppStore` + `RecordCodec` (SharedPreferences, no network) |
| Weekly view: last 7 days, average, highest day, trend | `WeeklyReport`, `HistoryPresenter` |
| 2D pet images + basic animation | `pet_*.png`, floating animation in `MainActivity` (speed and size follow the pet's state; off if the phone has animations turned off) |

Defaults chosen with the team: a "long session" is **60 minutes** in one app, the near-limit warning is at **80%** of the limit.

## Run in VS Code

1. Install the **Extension Pack for Java** and a JDK (8 or newer).
2. Open the `loser` folder (the one containing `src`) in VS Code.
3. Open `src/loser/Main.java` and press **Run** above `main`  (console menu).
4. Open `src/loser/test/AllTests.java` and press **Run** to run all tests.

Or from a terminal inside the `loser` folder:

```
javac -d out $(find src -name "*.java")      # Windows PowerShell: see note below
java -cp out loser.Main
java -cp out loser.test.AllTests
```
PowerShell: `javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName`

## Pet states (usage ratio = screen time / daily limit)

| State      | Ratio          |
|------------|----------------|
| Healthy    | under 50%      |
| Tired      | 50% to under 80% |
| Sick       | 80% to 100%    |
| Devastated | over 100%      |

## OOP concepts where to find them

- **Encapsulation**: private fields + validated getters/setters in `UsageRecord`, `Settings`, `Pet`.
- **Inheritance**: the four state classes extend `PetState`.
- **Abstraction**: `PetState` (abstract methods), `Notifiable` and `UsageSource` (interfaces).
- **Polymorphism**: `Pet.updateState()` calls `matches()` on each state; `Sick` and `Devastated`
  override it. `NotificationService.deliver()` is overridden on Android.
- **Exception handling**: `InvalidSettingsException`, `MissingPermissionException`, plus
  `IllegalArgumentException` for bad data; caught in `Main`.

## Putting the Android screen into Android Studio

1. **New project**: *Empty Views Activity*, language **Java**, **Package name `loser.android`**
   (this makes the generated `R` class live in the same package as the screens, so no imports
   are needed), minSdk **26** or higher.
2. Add Kotlin to the project (Android Studio offers this), and in `app/build.gradle` make sure
   these dependencies exist: `androidx.appcompat:appcompat`, `androidx.constraintlayout:constraintlayout`,
   `androidx.core:core-ktx` and `androidx.work:work-runtime` (2.9.x, for the background check).
   Set `android:name=".LoserApp"` is already in the manifest. Java 8 time classes (`java.time`) need minSdk 26 or core library desugaring.
3. Copy `src/loser/*` (everything except `Main.java` and `test/`) into `app/src/main/java/loser/`.
4. Copy the contents of `android/app/src/main/` over the project's `app/src/main/`
   (replace the generated `AndroidManifest.xml`; delete the generated `MainActivity` and layout).
5. **Images.** The four pet pictures are already in `res/drawable-nodpi/` (`pet_healthy`, `pet_tired`,
   `pet_sick`, `pet_devastated`; transparent PNG, 834 x 552 px, made from the team's own art).
   Three small files are **placeholders** so the project builds; replace them with the real exports
   (same file names, in `res/drawable/`) when they are ready. `<state>` is `healthy`, `tired`, `sick` or `devastated`:

   | File name                 | Status                                  | Figma layer (node ids: Healthy / Tired / Sick / Devastated)                      | Size in dp |
   |---------------------------|-----------------------------------------|-----------------------------------------------------------------------------------|------------|
   | `pet_<state>`             | done (4 files)                          | "Mochi feeling ..."                                                               | 278 x 184  |
   | `room_backdrop_<state>`   | placeholder for healthy only            | "Soft room backdrop" (2:2071 / 2:2169 / 2:2333 / 2:2498)                          | 256 x 256  |
   | `ic_condition_<state>`    | placeholder for healthy only            | heart / "Tired indicator" / "Sick indicator" / "Devastated indicator" (2:2076 / 2:2307 / 2:2472 / 2:2637) | 14 x 14 |
   | `ic_nav_pet` (all states) | placeholder paw icon                    | "My pet navigation icon" (10:76)                                                  | 24 x 24    |

   A state without its own backdrop or condition icon falls back to the healthy one.
   Easiest way to replace a placeholder: export the layer as **SVG**, then in Android Studio use
   *File > New > Vector Asset > Local file (SVG/PSD)* and keep the name.
6. Run on a real phone, grant **Usage access** when asked, and compare with the Figma frame.

### What comes from the design
Layout, spacing, colors, corner radii, font sizes and the Nunito fonts are taken from the
Figma frames "Mochi - Pet dashboard" (Healthy, 2:2046), "Mochi - Tired" (2:2147), "Mochi - Sick" (2:2311) and "Mochi - Devastated" (2:2476).
The phone status bar and the home bar in the mock-up are drawn by Android itself, so they are
not recreated. The pet-room color, the two cards, the sprite, the backdrop and the condition
icon all change with the pet state.

### Screen 2: "Your screen time" (Figma "Mochi - Today's usage", 7:31)
- Files: `UsageActivity`, `activity_usage.xml`, `item_category_row.xml`, `item_app_row.xml`;
  plain-Java logic in `loser.ui.UsagePresenter` / `UsageModel` (tested in `AllTests.usageScreen()`).
- `BaseScreenActivity` is the parent of both screens. It holds the bottom navigation
  (`view_bottom_nav.xml`, included by both layouts), system-bar handling and the usage reading.
- The pet icon in the navigation (`ic_nav_pet`) is tinted: purple when selected, grey when not.
  Export it once as a plain 24x24 shape and it works for both cases.
- Placeholders (not in the design): the "Other" category colors, and the color of the state word
  next to the total for Tired/Sick/Devastated. The headline "A little balance, a happier Mochi"
  and the lavender total card are only designed for Healthy, so they stay the same in every state.
- "Last 7 days" opens the Weekly history screen and "Edit limit" opens Limits & alerts.

### Screen 3: "Weekly history" (Figma "Mochi - Weekly history", 7:47)
- Files: `HistoryActivity`, `activity_history.xml`, `item_chart_day.xml`, `item_record_row.xml`;
  plain-Java logic in `loser.ui.HistoryPresenter` / `HistoryModel` (tested in `AllTests.historyScreen()`).
- Opened from the History tab, "Last 7 days" on the usage screen, or back from here with "‹ Screen time".
- Data: `AppSession.loadHistory()` reads the 7 complete days before today from the phone through
  `UsageSource.readUsageFor(day)` (Kotlin `UsageReader`). Android only keeps a few days of
  usage events, so older days may come back as 0 minutes.
- The average card shows the **trend** from `WeeklyReport.getTrend()` (the latest day compared with the earlier
  days of the week), as the proposal asks. The report keeps only the last 7 days (proposal, section IX).
- Chart scale follows the design: 0.45 dp per minute (4h = 108 dp). The limit line is placed from the
  data (3h = 81 dp above the bars' bottom); in the Figma frame it sits about 5 dp higher than that.
- Bars over the limit use the pink from the design (`bar_over`); a day exactly at the limit is "within".
- Placeholders (not in the design): the "Over limit" status and the "Trend: rising" text use the pink of the over-limit bar;
  the busy-week insight ("A busier week than planned", lavender card); the bars/records are tappable
  but only show "Day details are coming soon".
- In the frame the bars start 26 dp from the left edge of the chart card and end 42 dp from the right
  (not centered); it is reproduced as designed.

### Screen 4: "Limits & alerts" (Figma "Mochi - Limits & alerts", 7:63) = the Settings tab
- Files: `SettingsActivity`, `activity_settings.xml`, `AppStore`; plain-Java logic in
  `loser.ui.SettingsPresenter` / `SettingsModel` (tested in `AllTests.settingsScreen()`).
- Opened from the Settings tab, the "Limits & alerts" link on the pet screen and "Edit limit" on the usage screen.
- Hours and minutes boxes are real number inputs. "Save preferences" (or Done on the keyboard) checks the
  limit; if it is not allowed, nothing changes and the hint turns pink. The reminder preview updates while typing
  (it shows the 80% reminder, e.g. 2h 24m of 3h).
- Everything on the screen is saved between launches (`AppStore`, SharedPreferences, device only).
- Extra card "Your pet & your day" (not in the design, required by the proposal): pet name, time a new day starts
  (Android time picker), minutes of one-app use that count as a long session, and the three condition levels in % of the limit.
  All fields are checked together (`SettingsPresenter.applyAll`); if one is wrong nothing is changed and a pink message says why
  (levels must go up: tired < sick < devastated, maximum 300%).
- "Pet condition updates" = `Settings.notificationsOn` (already used by `NotificationService`).
  "Screen-time warnings" = `Settings.warningsOn`: the reminders at 80%, at the limit and for a long session.
- "Notifications allowed on this device" is read from Android; when notifications are off it shows a pink
  line instead, and tapping it opens the app's notification settings.
- "Device access & permissions" opens the system Usage access screen.
- Follows the proposal: the limit must be greater than zero (`Settings.MIN_LIMIT = 1`, maximum 24 hours).
  The hint text follows the constants.
- Placeholders (not in the design): the switch "off" colors and the invalid-input hint.

### Screen 5: "Connect your device" (Figma "Mochi - Connect your device", 7:79)
- Files: `ConnectActivity`, `activity_connect.xml`; plain-Java logic in `loser.ui.ConnectPresenter` / `ConnectModel`
  (tested in `AllTests.connectScreen()`).
- Opened from "Device access & permissions" on the Limits & alerts screen (back goes there; the Settings tab stays highlighted).
- Step 1 opens the Android Usage access settings; step 2 asks for the notification permission (pop-up on
  Android 13+, the app's notification settings before that, or after the user said no).
  Both refresh when the user comes back.
- "Continue" is unavailable until usage access is allowed (notifications are optional) and then goes to the pet screen.
- The welcome picture is the Healthy pet sprite (`pet_healthy`) at 178 x 118 dp.
- Placeholders (not in the design): the look once a step is done ("Allowed" in green, "Usage access allowed" /
  "Notifications enabled" buttons in the lavender color) and the available "Continue" (purple like "Save preferences").
- On the first launch this screen is the last step of the setup (no bottom navigation, Continue opens the pet).
