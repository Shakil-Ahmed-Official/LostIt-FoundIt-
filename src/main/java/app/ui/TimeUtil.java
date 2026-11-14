package app.ui;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

final class TimeUtil {

    private static final DateTimeFormatter SHORT =
            DateTimeFormatter.ofPattern("d MMM, HH:mm");

    private TimeUtil() { }

    /** Converts a database timestamp (UTC) to local time for display. */
    static String local(String databaseTime) {
        try {
            return LocalDateTime.parse(databaseTime.replace(' ', 'T'))
                    .atOffset(ZoneOffset.UTC)
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .format(SHORT);
        } catch (RuntimeException e) {
            return databaseTime;
        }
    }
}
