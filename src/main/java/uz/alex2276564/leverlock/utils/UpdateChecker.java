package uz.alex2276564.leverlock.utils;

import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.utils.runner.Runner;

import java.net.HttpURLConnection;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final String githubRepo;
    private final Runner runner;
    private final HttpUtils httpUtils;

    public UpdateChecker(JavaPlugin plugin, String githubRepo, Runner runner) {
        this.plugin = plugin;
        this.githubRepo = githubRepo;
        this.runner = runner;
        this.httpUtils = new HttpUtils();
    }

    public void checkForUpdates() {
        runner.runAsync(() -> {
            try {
                String latestVersion = getLatestVersion();
                String currentVersion = plugin.getDescription().getVersion();

                if (!latestVersion.equals(currentVersion)) {
                    plugin.getLogger().info("");
                    plugin.getLogger().info("New version available: " + latestVersion);
                    plugin.getLogger().info("You are running version: " + currentVersion);
                    plugin.getLogger().info("Download the latest version from: https://github.com/" + githubRepo + "/releases");
                    plugin.getLogger().info("");
                } else {
                    plugin.getLogger().info("You are running the latest version of " + plugin.getDescription().getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String getLatestVersion() throws Exception {
        String apiUrl = "https://api.github.com/repos/" + githubRepo + "/releases/latest";
        HttpUtils.HttpResponse response = httpUtils.getResponse(apiUrl, "MinecraftPlugin");

        if (response.responseCode() == HttpURLConnection.HTTP_OK) {
            JsonObject jsonObject = response.jsonBody();
            return jsonObject.get("tag_name").getAsString();
        } else {
            throw new Exception("GitHub API returned HTTP " + response.responseCode());
        }
    }
}
