package uz.alex2276564.leverlock.utils;

import com.alibaba.fastjson2.JSONObject;
import uz.alex2276564.leverlock.utils.runner.Runner;

import java.util.logging.Logger;

public class UpdateChecker {

    private final String pluginName;
    private final String currentVersion;
    private final String githubRepo;
    private final Runner runner;
    private final HttpUtils httpUtils;
    private final String userAgent;
    private final Logger logger;

    public UpdateChecker(String pluginName,
                         String currentVersion,
                         String githubRepo,
                         Runner runner,
                         HttpUtils httpUtils,
                         Logger logger) {
        this.pluginName = pluginName;
        this.currentVersion = currentVersion;
        this.githubRepo = githubRepo;
        this.runner = runner;
        this.httpUtils = httpUtils;
        this.logger = logger;
        this.userAgent = pluginName + "/" + currentVersion;
    }

    public void checkForUpdates() {
        runner.runAsync(() -> {
            try {
                String latestVersion = getLatestVersion();

                if (!latestVersion.equals(currentVersion)) {
                    logger.info("");
                    logger.info("New version available: " + latestVersion);
                    logger.info("You are running version: " + currentVersion);
                    logger.info("Download the latest version from: https://github.com/" + githubRepo + "/releases");
                    logger.info("");
                } else {
                    logger.info("You are running the latest version of " + pluginName);
                }
            } catch (Exception e) {
                logger.warning("Failed to check for updates: " + e.getMessage());
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