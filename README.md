# LeverLock 🔒

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.16.5+-brightgreen)](https://papermc.io/software/paper)
[![Java Version](https://img.shields.io/badge/java-16+-orange)](https://adoptium.net/installation/linux/)
[![GitHub Release](https://img.shields.io/github/v/release/alex2276564/LeverLock?color=blue)](https://github.com/alex2276564/LeverLock/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

**LeverLock** is a Minecraft plugin designed to prevent rapid lever interactions, which can potentially cause lag or be exploited for unintended game mechanics. It works as a complementary tool to [AntiRedstoneClock-Remastered](https://modrinth.com/plugin/antiredstoneclock-remastered), providing comprehensive protection against redstone-based lag and exploits.

## ✨ Features

* **Prevents Rapid Lever Interactions:**  Implements a cooldown to prevent players from toggling levers too quickly.
* **Customizable Cooldown:** Configure the cooldown duration to suit your server's needs.
* **Customizable Messages:**  Personalize the message players see when they trigger the cooldown.
* **Memory Efficient:**  Regularly cleans up stored data to minimize memory usage.
* **Reload Command:**  Easily reload the configuration without restarting the server.
* **Lightweight and Efficient:** Minimal performance impact on your server.
* **Compatible with AntiRedstoneClock-Remastered:** Enhances protection against redstone-based exploits and lag.

## 📥 Installation

1. **Download:** Download the latest version of LeverLock from the [Releases](https://github.com/alex2276564/LeverLock/releases) page.
2. **Install:** Place the `.jar` file into your server's `plugins` folder.
3. **Optional - Enhanced Protection:** Download and install [AntiRedstoneClock-Remastered](https://modrinth.com/plugin/antiredstoneclock-remastered) for comprehensive protection against redstone-related lag and exploits.  Place the `.jar` file into your server's `plugins` folder.
4. **Restart:** Restart your server to load the plugin(s).

## 🛠️ Configuration

Edit the `config.yml` file in the plugin's folder to customize settings:

```yaml
# LeverLock Configuration

# Cooldown settings
cooldown:
  # Duration in seconds before a player can interact with a lever again
  duration: 1

# Message settings
message:
  # Message displayed when a player tries to interact with a lever too quickly
  cooldown: "§cYou are interacting with the lever too quickly!"

# Cleanup settings
cleanup:
  # Interval in seconds for cleaning up the cooldown data (5 minutes by default)
  interval: 300
```

## 📜 Commands

- `/leverlockreload` - Reloads the plugin configuration (requires `leverlock.reload` permission)

## 🛠️ Compatibility

- **Minecraft Versions:** 1.16.5 to the latest release
- **Server Software:** [Paper](https://papermc.io/) (1.16.5 and newer)
- **Complementary Plugins:** [AntiRedstoneClock-Remastered](https://modrinth.com/plugin/antiredstoneclock-remastered)

## 🆘 Support

If you encounter any issues or have suggestions for improving the plugin, please create an [issue](https://github.com/alex2276564/LeverLock/issues) in this repository.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

[Alex] - [https://github.com/alex2276564]

We appreciate your contribution to the project! If you like this plugin, please give it a star on GitHub.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/alex2276564/LeverLock/issues).

### How to Contribute

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/YourFeature`).
3. Commit your changes (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a Pull Request.

---

Thank you for using **LeverLock**! We hope it makes your gaming experience more stable and enjoyable. 🎮🔥