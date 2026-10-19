package uz.alex2276564.leverlock.utils;

import lombok.experimental.UtilityClass;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Generic time utilities.
 * <p>
 * Responsibilities:
 * - provide shared clocks for server-local time and UTC;
 * - expose commonly used date/time patterns (dd.MM.yyyy, ISO-8601, etc.);
 * - offer small helpers for getting "now" and formatting it.
 */
@UtilityClass
public class TimeUtils {

    /**
     * Server-local clock (JVM default time zone).
     */
    public static final Clock SYSTEM_CLOCK = Clock.systemDefaultZone();

    /**
     * UTC clock.
     */
    public static final Clock UTC_CLOCK = Clock.systemUTC();

    // =====================================================================
    // Common formatters (immutable, thread-safe)
    // =====================================================================

    /**
     * Date in dd.MM.yyyy, e.g. "24.05.2025".
     */
    public static final DateTimeFormatter DATE_DMY =
            DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT);

    /**
     * Date-time in dd.MM.yyyy HH:mm:ss, e.g. "24.05.2025 14:30:05".
     */
    public static final DateTimeFormatter DATE_TIME_DMY_HMS =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.ROOT);

    /**
     * Date-time in dd.MM.yyyy_HH-mm-ss, e.g. "24.05.2025_14-30-05"
     * (safe for filenames).
     */
    public static final DateTimeFormatter DATE_TIME_DMY_HMS_FILENAME =
            DateTimeFormatter.ofPattern("dd.MM.yyyy_HH-mm-ss", Locale.ROOT);

    /**
     * Standard ISO-8601 with offset, e.g. "2025-05-24T14:30:05+05:00".
     */
    public static final DateTimeFormatter ISO_OFFSET =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    // =====================================================================
    // "Now" helpers
    // =====================================================================

    /**
     * Current time in server-local time zone.
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now(SYSTEM_CLOCK);
    }

    /**
     * Current time in UTC.
     */
    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(UTC_CLOCK);
    }

    /**
     * Current date in server-local time zone.
     */
    public static LocalDate today() {
        return LocalDate.now(SYSTEM_CLOCK);
    }

    /**
     * Current local date-time (no zone object attached), server-local time.
     */
    public static LocalDateTime nowLocal() {
        return LocalDateTime.now(SYSTEM_CLOCK);
    }

    // =====================================================================
    // Formatting helpers
    // =====================================================================

    /**
     * Format as "dd.MM.yyyy HH:mm:ss".
     */
    public static String formatDmyHms(ZonedDateTime dt) {
        return DATE_TIME_DMY_HMS.format(dt);
    }

    /**
     * Format as ISO-8601 with offset.
     */
    public static String formatIsoOffset(ZonedDateTime dt) {
        return ISO_OFFSET.format(dt);
    }
}