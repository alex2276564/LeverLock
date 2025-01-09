package uz.alex2276564.leverlock.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final String githubRepo;

    public UpdateChecker(JavaPlugin plugin, String githubRepo) {
        this.plugin = plugin;
        this.githubRepo = githubRepo;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String latestVersion = getLatestVersion();
                if (latestVersion != null && !latestVersion.equals(plugin.getDescription().getVersion())) {
                    plugin.getLogger().info("");
                    plugin.getLogger().info("New version available: " + latestVersion);
                    plugin.getLogger().info("You are running version: " + plugin.getDescription().getVersion());
                    plugin.getLogger().info("Download the latest version from: https://github.com/" + githubRepo + "/releases");
                    plugin.getLogger().info("");
                } else {
                    plugin.getLogger().info("You are running the latest version of " + plugin.getDescription().getName());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String getLatestVersion() throws IOException {
        String apiUrl = "https://api.github.com/repos/" + githubRepo + "/releases/latest";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "MinecraftPlugin");

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to check for updates: HTTP " + connection.getResponseCode());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return jsonObject.get("tag_name").getAsString();
        }
    }
}
