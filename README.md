# LeverLock 🔒

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.16.5+-brightgreen)](https://papermc.io/software/paper)
[![Java Version](https://img.shields.io/badge/java-17+-orange)](https://adoptium.net/installation/linux/)
[![GitHub Release](https://img.shields.io/github/v/release/alex2276564/LeverLock?color=blue)](https://github.com/alex2276564/LeverLock/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Text Formatting](https://img.shields.io/badge/Text%20Formatting-🌈%20MiniMessage-ff69b4)](https://docs.advntr.dev/minimessage/)

**LeverLock** is a Minecraft plugin designed to prevent rapid lever interactions, which can potentially cause lag or be exploited for unintended game mechanics. It works as a complementary tool to [AntiRedstoneClock-Remastered](https://modrinth.com/plugin/antiredstoneclock-remastered), providing comprehensive protection against redstone-based lag and exploits.

## ✨ Features

* **Prevents Rapid Lever Interactions:**  Implements a cooldown to prevent players from toggling levers too quickly.
* **Customizable Cooldown:** Configure the cooldown duration to suit your server's needs.
* **Customizable Messages:**  Personalize the message players see when they trigger the cooldown.
* **Memory Efficient:**  Regularly cleans up stored data to minimize memory usage.
* **Reload Command:**  Easily reload the configuration without restarting the server.
* **Lightweight and Efficient:** Minimal performance impact on your server.
* **Compatible with AntiRedstoneClock-Remastered:** Enhances protection against redstone-based exploits and lag.
* **Auto-Update Check:** On server start, the plugin checks for updates. If a new version is available, a notification is displayed in the console.
* **Modern Text Rendering:** Uses Adventure MiniMessage for sleek formatting on supported servers (Paper 1.18+), with automatic fallback on older versions.

## ⚠️ Important Notes About Redstone Exploits

Using `use-faster-eigencraft-redstone: true` in legacy versions or `redstone-implementation: ALTERNATE_CURRENT` in newer versions of Paper **will not protect your server from all redstone-based exploits**. These settings may optimize performance but do not prevent all exploit scenarios.

To ensure maximum protection against redstone-related exploits and lag, it is recommended to use **LeverLock** alongside [AntiRedstoneClock-Remastered](https://modrinth.com/plugin/antiredstoneclock-remastered), as both plugins provide comprehensive safeguards beyond what Paper's settings offer.

Make sure to configure your server with the appropriate plugins for full protection!

If you are using the [AnarchyExploitFixes](https://github.com/xGinko/AnarchyExploitFixes) plugin, which also includes redstone and lever checks, you may not need to install LeverLock and AntiRedstoneClockRemastered. However, consider the following:

- LeverLock and AntiRedstoneClockRemastered were specifically developed to counter particular exploits
- AnarchyExploitFixes contains many different fixes, and its redstone checks might be less tested
- It's recommended to test both options on your server to determine the most effective solution

The choice between specialized plugins (LeverLock + AntiRedstoneClockRemastered) and a comprehensive solution (AnarchyExploitFixes) depends on your server's specific needs and testing results.

## 📥 Installation

1. **Download:** Download the latest version of LeverLock from the [Releases](https://github.com/alex2276564/LeverLock/releases) page.
2. **Install:** Place the `.jar` file into your server's `plugins` folder.
3. **Optional - Enhanced Protection:** Download and install [AntiRedstoneClock-Remastered](https://modrinth.com/plugin/antiredstoneclock-remastered) for comprehensive protection against redstone-related lag and exploits.  Place the `.jar` file into your server's `plugins` folder.
4. **Restart:** Restart your server to load the plugin(s).

## 📜 Commands

LeverLock supports both the full command `/leverlock` and the shorter alias `/ll` for all commands (requires `leverlock.command` permission).

- `/ll help` - Show help information (requires `leverlock.command`)
- `/ll reload` - Reloads the plugin configuration (requires `leverlock.reload` permission)

## 🛠️ Compatibility

- **Minecraft Versions:** 1.16.5 to the latest release
- **Server Software:**
    - ✅ [Paper](https://papermc.io/) (1.16.5 and newer) - **Fully Supported**
    - ✅ [Folia](https://papermc.io/software/folia) - **Fully Supported** with optimized region-aware scheduling
    - ❌ Spigot - Not supported
- **Complementary Plugins:** [AntiRedstoneClock-Remastered](https://modrinth.com/plugin/antiredstoneclock-remastered)

## 📝 Note

**Native MiniMessage Support:** Plugin uses only native Kyori Adventure MiniMessage implementation without any backporting or compatibility layers:

- **Paper 1.18+:** Full native MiniMessage support with all features including gradients, hover effects, click events, and advanced formatting
- **Paper 1.16-1.17:** Partial support with automatic conversion to legacy ChatColor codes. Supported features include basic colors (`<red>`, `<blue>`, etc.), text styles (`<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`, `<obfuscated>`), and reset tags (`<reset>`). Advanced features like gradients and hover effects are automatically stripped without causing errors.

You can use the [MiniMessage Web Editor](https://webui.advntr.dev/) to test and preview your formatting. The plugin will automatically adapt the formatting to your server's capabilities, so you can use the same configuration across different server versions.

> Note: On Paper 1.16–1.17 there is no perfect way to get every MiniMessage feature without adding extra, version‑sensitive libraries. This plugin intentionally does **not** use full Adventure backports such as `BukkitAudiences` (they require constant updates and can conflict with complex plugins like ViaVersion). Instead, legacy servers get a simple MiniMessage‑like formatter (colors/styles only), while modern servers use the native Paper Adventure stack with full features.

## 📦 Other Plugins

Also check out my other plugins for protecting your Minecraft server:

- [**PermGuard**](https://github.com/alex2276564/PermGuard)  
  *PermGuard* - a plugin to enhance server security. It temporarily revokes admin permissions when a player joins the server and sending security alerts to Telegram, to prevent unauthorized access or potential security breaches. Admins can only restore permissions manually via the console using commands like `lp user playernick permission set *`.

- [**NoMoreTNTChainCrash**](https://github.com/alex2276564/NoMoreTNTChainCrash)  
  *NoMoreTNTChainCrash* is a Minecraft plugin designed to prevent server crashes and lag caused by excessive TNT explosions. It achieves this by ignoring TNT before automated chain explosions can occur, while still allowing players to manually detonate TNT as desired.

> 🔍 **You can find more of my Minecraft plugins here:**  
> [https://github.com/alex2276564?tab=repositories](https://github.com/alex2276564?tab=repositories)

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