package uz.alex2276564.leverlock.utils;

import com.alibaba.fastjson2.JSONObject;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.utils.runner.Runner;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final String githubRepo;
    private final Runner runner;
    private final HttpUtils httpUtils;
    private final String userAgent;

    public UpdateChecker(JavaPlugin plugin, String githubRepo, Runner runner, HttpUtils httpUtils) {
        this.plugin = plugin;
        this.githubRepo = githubRepo;
        this.runner = runner;
        this.httpUtils = httpUtils;
        this.userAgent = plugin.getDescription().getName()
                + "/" + plugin.getDescription().getVersion();
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

        HttpUtils.HttpResponse response = httpUtils.getJson(apiUrl, userAgent);

        if (response.statusCode() == 200) {
            JSONObject jsonObject = response.jsonBody();
            if (jsonObject.isEmpty()) {
                throw new Exception("GitHub API returned empty or invalid JSON body");
            }

            String tagName = jsonObject.getString("tag_name");
            if (tagName == null) {
                throw new Exception("GitHub API response does not contain tag_name");
            }

            return SecurityUtils.sanitize(tagName, SecurityUtils.SanitizeType.VERSION);
        } else {
            throw new Exception("GitHub API returned HTTP " + response.statusCode());
        }
    }
}