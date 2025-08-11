package uz.alex2276564.leverlock.utils.backup;

import org.jetbrains.annotations.NotNull;
import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.utils.runner.Runner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class BackupManager {
    private static final String BACKUPS_DIR_NAME = "backups";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT);
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH-mm-ss", Locale.ROOT);
    private static final int BACKUP_INTERVAL_DAYS = 30;

    private final LeverLock plugin;
    private final Runner runner;
    private final Path dataDir;
    private final Path backupsDir;

    public BackupManager(@NotNull LeverLock plugin) {
        this.plugin = plugin;
        this.runner = plugin.getRunner();
        this.dataDir = plugin.getDataFolder().toPath();
        this.backupsDir = dataDir.resolve(BACKUPS_DIR_NAME);
    }

    /**
     * Check and create backup if needed (async)
     */
    public void checkAndBackupAsync() {
        runner.runAsync(() -> {
            try {
                checkAndBackup();
            } catch (Exception e) {
                plugin.getLogger().severe("Backup check failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Force create backup now (async)
     */
    public void forceBackupAsync() {
        runner.runAsync(() -> {
            try {
                plugin.getLogger().info("Starting forced backup...");
                createBackup();
            } catch (Exception e) {
                plugin.getLogger().severe("Forced backup failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void checkAndBackup() throws IOException {
        // Create backups directory if needed
        if (Files.notExists(backupsDir)) {
            Files.createDirectories(backupsDir);
            plugin.getLogger().info("Created backups directory");
        }

        Optional<LocalDate> lastBackup = findLastValidBackupDate();
        LocalDate now = LocalDate.now();
        LocalDate threshold = now.minusDays(BACKUP_INTERVAL_DAYS);

        if (lastBackup.isEmpty()) {
            plugin.getLogger().info("No valid backups found. Creating initial backup...");
            createBackup();
        } else if (lastBackup.get().isBefore(threshold)) {
            long daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastBackup.get(), now);
            plugin.getLogger().info("Last backup was " + daysSince + " days ago. Creating new backup...");
            createBackup();
        } else {
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, lastBackup.get().plusDays(BACKUP_INTERVAL_DAYS));
            plugin.getLogger().info("Latest backup is recent. Next backup in " + daysRemaining + " days");
        }
    }

    private Optional<LocalDate> findLastValidBackupDate() throws IOException {
        if (Files.notExists(backupsDir)) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();

        try (Stream<Path> stream = Files.list(backupsDir)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> parseBackupDate(path.getFileName().toString()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(date -> !date.isAfter(today)) // Ignore future dates
                    .max(Comparator.naturalOrder());
        }
    }

    private Optional<LocalDate> parseBackupDate(String dirName) {
        // Try parsing as full datetime first (dd.MM.yyyy_HH-mm-ss)
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dirName, DATE_TIME_FORMAT);
            return Optional.of(dateTime.toLocalDate());
        } catch (Exception ignored) {
            // Try parsing as date only (dd.MM.yyyy)
            try {
                return Optional.of(LocalDate.parse(dirName, DATE_FORMAT));
            } catch (Exception ignored2) {
                return Optional.empty();
            }
        }
    }

    private void createBackup() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String backupName = DATE_FORMAT.format(now);
        Path backupPath = backupsDir.resolve(backupName);

        // If backup already exists today, add time suffix
        if (Files.exists(backupPath)) {
            backupName = DATE_TIME_FORMAT.format(now);
            backupPath = backupsDir.resolve(backupName);

            // Extra safety: ensure unique name
            int counter = 1;
            while (Files.exists(backupPath)) {
                backupPath = backupsDir.resolve(backupName + "_" + counter);
                counter++;
            }
        }

        Files.createDirectories(backupPath);

        long startTime = System.currentTimeMillis();
        BackupStats stats = copyDirectory(dataDir, backupPath);

        long duration = System.currentTimeMillis() - startTime;
        plugin.getLogger().info(String.format(
                "Backup completed: %s (%d files, %d dirs, %.2f MB, %d ms)",
                backupPath.getFileName(),
                stats.files,
                stats.directories,
                stats.totalSize / (1024.0 * 1024.0),
                duration
        ));
    }

    private BackupStats copyDirectory(Path source, Path target) throws IOException {
        BackupStats stats = new BackupStats();

        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
                // Skip the backups directory itself
                if (dir.equals(backupsDir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                stats.directories++;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                // Double-check: skip files in backups directory
                if (file.startsWith(backupsDir)) {
                    return FileVisitResult.CONTINUE;
                }

                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, REPLACE_EXISTING, COPY_ATTRIBUTES);
                stats.files++;
                stats.totalSize += attrs.size();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFileFailed(@NotNull Path file, @NotNull IOException exc) {
                plugin.getLogger().warning("Failed to backup file: " + file + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });

        return stats;
    }

    private static class BackupStats {
        long files = 0;
        long directories = 0;
        long totalSize = 0;
    }
}