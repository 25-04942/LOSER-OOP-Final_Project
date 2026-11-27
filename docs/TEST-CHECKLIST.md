# LOS:ER test checklist

Run on a real Android phone (emulators have almost no usage data). Tick each line and take a screenshot where marked 📷.

## Automatic tests (PC)
- [ ] `javac -encoding UTF-8 -d out $(find src -name "*.java")` compiles with no errors
- [ ] `java -cp out loser.test.AllTests` prints `174 passed, 0 failed`

## First launch (proposal Phase 1)
- [ ] App opens on the **setup** screen, not the pet 📷
- [ ] Empty pet name, limit 0, or a name longer than 20 characters shows a pink message and does not continue
- [ ] Valid name + limit + day start → **Connect** screen (no bottom navigation, no back link) 📷
- [ ] Continue stays disabled until Usage access is allowed in Android settings
- [ ] Enable notifications button asks for the permission (Android 13+)
- [ ] Continue → pet screen; closing and reopening the app does **not** show setup again

## Pet and usage
- [ ] Pet picture, room color and speech match the state (Healthy / Tired / Sick / Devastated) 📷 (test by setting a small limit, e.g. 1 minute)
- [ ] Pet floats gently; Sick moves faster and less than Healthy; no movement when animations are off in Android
- [ ] "Your screen time" lists apps and categories and the total equals the pet screen total 📷

## Settings (proposal: limit, reset time, notification options, adjustable thresholds)
- [ ] Limit 0 minutes is refused; 24 hours is accepted
- [ ] Change the day start time; the note under the limit shows the new time
- [ ] Levels must go up (tired < sick < devastated); e.g. 60 / 50 / 100 is refused and **nothing** changes
- [ ] Change levels (e.g. 30 / 60 / 90) → the pet state changes accordingly
- [ ] Rename the pet → name changes on every screen and in notifications
- [ ] Close the app completely and reopen: all settings are still there 📷

## Notifications
- [ ] At 80% of the limit: one reminder "You've used … of your … limit"
- [ ] At 100%: one reminder "You've reached your daily limit"
- [ ] One app open for longer than the long-session setting (default 60 min; set to 1 to test): one reminder
- [ ] Pet state change: one notification (turn "Pet condition updates" off → none)
- [ ] "Screen-time warnings" off → no 80% / limit / long-session reminders
- [ ] Each alert appears only **once per day**, also after force-closing the app
- [ ] With the app closed, a reminder still arrives (background check runs about every 15 minutes) 📷
- [ ] Tapping a notification opens the pet screen

## Daily reset and history
- [ ] After the reset time passes, today's total starts again from 0 and the pet is Healthy
- [ ] Yesterday appears in Weekly history with its total and the pet's state that day
- [ ] Weekly history shows at most 7 days, the daily average, the highest day and the trend 📷
- [ ] Records stay after restarting the phone; nothing needs the internet

## Permissions
- [ ] Turning Usage access off in Android: pet screen says "Usage access needed" and the app does not crash
- [ ] Turning notifications off in Android: Settings shows the pink "Notifications are turned off" line
