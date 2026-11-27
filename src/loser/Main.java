package loser;

import loser.core.Pet;
import loser.core.Settings;
import loser.core.UsageTracker;
import loser.core.WeeklyReport;
import loser.exception.InvalidSettingsException;
import loser.exception.MissingPermissionException;
import loser.model.Category;
import loser.model.DailyRecord;
import loser.model.UsageRecord;
import loser.notification.NotificationService;
import loser.source.SampleUsageSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Console menu for testing the LOS:ER logic before the Android UI exists. */
public class Main {
    private final Settings settings = new Settings();
    private final UsageTracker tracker = new UsageTracker(settings);
    private final Pet pet = new Pet("Lo");
    private final NotificationService notifier = new NotificationService(settings);
    private final WeeklyReport weekly = new WeeklyReport();
    private final SampleUsageSource source = new SampleUsageSource(tracker.getDate());
    private final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("=== LOS:ER (Life Observer System: Endangered Reality) ===");
        boolean running = true;
        while (running) {
            printMenu();
            if (!in.hasNextLine()) break;
            String choice = in.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": syncFromSource(); break;
                case "2": addManualUsage(); break;
                case "3": showSummary(); break;
                case "4": showPet(); break;
                case "5": settingsMenu(); break;
                case "6": endDay(); break;
                case "7": System.out.println(weekly.displayWeek()); break;
                case "8": showNotifications(); break;
                case "9": togglePermission(); break;
                case "10": loadDemoWeek(); break;
                case "0": running = false; break;
                default: System.out.println("Unknown option.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("\n----- MENU (" + tracker.getDate() + ") -----");
        System.out.println(" 1  Sync usage from phone (sample data)");
        System.out.println(" 2  Add usage manually");
        System.out.println(" 3  Today's usage summary");
        System.out.println(" 4  View pet");
        System.out.println(" 5  Settings");
        System.out.println(" 6  End day (save and reset)");
        System.out.println(" 7  Weekly report");
        System.out.println(" 8  Notification history");
        System.out.println(" 9  Toggle usage-access permission (currently "
                + (source.isPermissionGranted() ? "GRANTED" : "DENIED") + ")");
        System.out.println("10  Load demo week");
        System.out.println(" 0  Exit");
        System.out.print("Choose: ");
    }

    // ---------- actions ----------
    private void syncFromSource() {
        try {
            List<UsageRecord> fresh = source.readTodayUsage();
            tracker.replaceAll(fresh);
            System.out.println("Loaded " + fresh.size() + " apps.");
            refreshPet();
        } catch (MissingPermissionException e) {
            System.out.println("Cannot read usage: " + e.getMessage());
        }
    }

    private void addManualUsage() {
        System.out.print("App name: ");
        String name = in.nextLine();
        Category category = readCategory();
        int minutes = readInt("Minutes used: ");
        int longest = readInt("Longest session (minutes): ");
        try {
            tracker.addUsage(new UsageRecord(name, category, minutes, longest, tracker.getDate()));
            System.out.println("Added.");
            refreshPet();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private void showSummary() {
        if (tracker.isEmpty()) {
            System.out.println("No usage recorded today.");
            return;
        }
        System.out.println("=== Today's usage ===");
        for (UsageRecord r : tracker.getRecords()) System.out.println("  " + r);
        System.out.println("By category:");
        for (Map.Entry<Category, Integer> e : tracker.getTotalByCategory().entrySet()) {
            if (e.getValue() > 0) System.out.println("  " + e.getKey() + ": " + e.getValue() + " min");
        }
        System.out.println("Total: " + tracker.getTotalScreenTime() + " min of "
                + settings.getDailyLimit() + " min (" + Math.round(tracker.getRatio() * 100) + "%)");
        System.out.println("Most used: " + tracker.getMostUsedApp().getAppName());
        System.out.println("Longest session: " + tracker.getLongestSession() + " min");
    }

    private void showPet() {
        System.out.println(pet);
        System.out.println("  " + pet.getName() + " says: \"" + pet.getMessage() + "\"");
        System.out.println("  HP [" + bar(pet.getHealthPoints(), Pet.MAX_HEALTH, 20) + "]");
        System.out.println("  Usage: " + Math.round(tracker.getRatio() * 100) + "% of daily limit");
    }

    private void settingsMenu() {
        System.out.println(settings);
        System.out.println("1 Change daily limit | 2 Toggle notifications | 3 Change reset time | other: back");
        System.out.print("Choose: ");
        String c = in.nextLine().trim();
        try {
            if (c.equals("1")) {
                settings.setDailyLimit(readInt("New daily limit (minutes): "));
                refreshPet();
            } else if (c.equals("2")) {
                settings.setNotificationsOn(!settings.isNotificationsOn());
            } else if (c.equals("3")) {
                System.out.print("New reset time (HH:mm): ");
                settings.setResetTime(LocalTime.parse(in.nextLine().trim()));
            }
            System.out.println(settings);
        } catch (InvalidSettingsException e) {
            System.out.println("Invalid setting: " + e.getMessage());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid time format. Use HH:mm, e.g. 06:00.");
        }
    }

    private void endDay() {
        DailyRecord today = tracker.createDailyRecord(pet.getCurrentState().getStateName());
        weekly.addDay(today);
        System.out.println("Saved: " + today);
        LocalDate next = tracker.getDate().plusDays(1);
        tracker.reset(next);
        source.setDate(next);
        pet.resetPet();
        System.out.println("New day: " + next + ". " + pet.getName() + " is fresh again.");
    }

    private void showNotifications() {
        if (notifier.getHistory().isEmpty()) {
            System.out.println("No notifications yet.");
            return;
        }
        for (String n : notifier.getHistory()) System.out.println("  " + n);
    }

    private void togglePermission() {
        source.setPermissionGranted(!source.isPermissionGranted());
        System.out.println("Permission is now " + (source.isPermissionGranted() ? "GRANTED" : "DENIED"));
    }

    private void loadDemoWeek() {
        int[] minutes = {120, 200, 150, 90, 210, 175};
        Pet temp = new Pet("temp");
        for (int i = 0; i < minutes.length; i++) {
            LocalDate d = tracker.getDate().minusDays(minutes.length - i);
            Map<Category, Integer> cats = new EnumMap<Category, Integer>(Category.class);
            cats.put(Category.SOCIAL, minutes[i] / 2);
            cats.put(Category.GAMES, minutes[i] / 4);
            cats.put(Category.STUDY, minutes[i] - minutes[i] / 2 - minutes[i] / 4);
            temp.updateState((double) minutes[i] / settings.getDailyLimit());
            weekly.addDay(new DailyRecord(d, minutes[i], cats, temp.getCurrentState().getStateName()));
        }
        System.out.println("Demo week loaded. Choose 7 to view it.");
    }

    // ---------- helpers ----------
    private void refreshPet() {
        boolean changed = pet.updateState(tracker.getRatio());
        if (changed) notifier.notifyStateChange(pet.getCurrentState());
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private Category readCategory() {
        Category[] all = Category.values();
        for (int i = 0; i < all.length; i++) System.out.println("  " + (i + 1) + ") " + all[i]);
        while (true) {
            int n = readInt("Category number: ");
            if (n >= 1 && n <= all.length) return all[n - 1];
            System.out.println("Choose 1-" + all.length + ".");
        }
    }

    private String bar(int value, int max, int width) {
        int filled = Math.round((float) value / max * width);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append(i < filled ? '#' : '-');
        return sb.toString();
    }
}
