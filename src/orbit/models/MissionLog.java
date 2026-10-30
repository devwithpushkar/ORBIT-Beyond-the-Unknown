package orbit.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MissionLog maintains a chronological audit record of significant flight operations,
 * interplanetary voyages, surface expeditions, resource recoveries, hazards,
 * and mission milestones.
 */
public class MissionLog {

    /**
     * Individual log entry with chronological identifier, classification, and narrative detail.
     */
    public static class LogEntry {
        private final String timestamp;
        private final String category;
        private final String description;

        public LogEntry(String timestamp, String category, String description) {
            this.timestamp = (timestamp != null) ? timestamp.trim() : "LOG-000";
            this.category = (category != null) ? category.trim() : "GENERAL";
            this.description = (description != null) ? description.trim() : "";
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "[" + timestamp + "] [" + category + "] " + description;
        }
    }

    private final List<LogEntry> entries;
    private int counter;

    public MissionLog() {
        this.entries = new ArrayList<>();
        this.counter = 1;
    }

    /**
     * Adds an entry to the log in chronological order.
     * Prevents consecutive identical log spamming.
     */
    public synchronized void addEntry(String category, String description) {
        if (description == null || description.trim().isEmpty()) {
            return;
        }
        String cleanCat = (category != null) ? category.trim().toUpperCase() : "GENERAL";
        String cleanDesc = description.trim();

        // Prevent duplicate consecutive entries
        if (!entries.isEmpty()) {
            LogEntry last = entries.get(entries.size() - 1);
            if (last.getCategory().equalsIgnoreCase(cleanCat) && last.getDescription().equalsIgnoreCase(cleanDesc)) {
                return;
            }
        }

        String timestamp = String.format("LOG-%03d", counter++);
        entries.add(new LogEntry(timestamp, cleanCat, cleanDesc));
    }

    /**
     * Appends a log entry directly with a preserved timestamp (used during game state loading).
     */
    public synchronized void addLoadedEntry(String timestamp, String category, String description) {
        if (description == null || description.trim().isEmpty()) {
            return;
        }
        String cleanTime = (timestamp != null && !timestamp.trim().isEmpty()) ? timestamp.trim() : String.format("LOG-%03d", counter++);
        String cleanCat = (category != null) ? category.trim().toUpperCase() : "GENERAL";
        String cleanDesc = description.trim();
        entries.add(new LogEntry(cleanTime, cleanCat, cleanDesc));

        if (cleanTime.startsWith("LOG-")) {
            try {
                int num = Integer.parseInt(cleanTime.substring(4));
                if (num >= counter) {
                    counter = num + 1;
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    /**
     * Returns an unmodifiable list of all logged events in chronological order (oldest to newest).
     */
    public List<LogEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns an unmodifiable list of entries reversed (newest first).
     */
    public List<LogEntry> getEntriesNewestFirst() {
        List<LogEntry> reversed = new ArrayList<>(entries);
        Collections.reverse(reversed);
        return Collections.unmodifiableList(reversed);
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Clears all log entries and resets chronological counter.
     */
    public synchronized void clear() {
        this.entries.clear();
        this.counter = 1;
    }
}
