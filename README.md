<div align="center">

# 🏷️ Holograms For Allay

**Powerful and Flexible Hologram System for Allay**

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-green.svg)
![Platform](https://img.shields.io/badge/Platform-Allay-orange.svg)

<br>

[![Русский](https://img.shields.io/badge/Язык-Русский-red?style=for-the-badge&logo=google-translate&logoColor=white)](README_RU.md)

</div>

---

**Holograms** is a feature-rich hologram plugin for **Allay** servers. Create floating text displays with multi-line support, per-player localization, and PlaceholderAPI integration. Perfect for server info, rules, welcomes, and dynamic displays.

### ✨ Features
- **Easy Creation**: GUI-based hologram creation and editing via forms.
- **Multi-line Support**: Create holograms with multiple floating text lines.
- **Per-locale Translations**: Different text for different player languages.
- **PlaceholderAPI Support**: Dynamic placeholders like player names, stats, etc.
- **Auto-update**: Configurable refresh interval for dynamic content.
- **Position Control**: Set exact coordinates or use current position.
- **Persistent Storage**: Holograms saved in JSON format.
- **Performance**: Optimized with per-player rendering system.

### 🎮 Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/holo create` | `holograms.admin` | Open hologram creation form. |
| `/holo edit [name]` | `holograms.admin` | Edit a hologram (nearest if no name). |
| `/holo delete [name]` | `holograms.admin` | Delete a hologram. |
| `/holo list` | `holograms.admin` | List all holograms. |
| `/holo tp <name>` | `holograms.admin` | Teleport to a hologram. |
| `/holo locale <name>` | `holograms.admin` | Manage hologram translations. |
| `/holo reload` | `holograms.admin` | Reload all holograms. |

### 📝 Hologram Options
| Option | Description |
|--------|-------------|
| **Name** | Unique identifier for the hologram. |
| **Lines** | Text content (use `\n` for line breaks). |
| **Multi-line** | Each line as separate floating text. |
| **Update Interval** | Seconds between placeholder refreshes (-1 = disabled). |
| **Line Spacing** | Distance between multi-line texts. |
| **Position** | X, Y, Z coordinates in the world. |

### 🌍 Localization
Create different text for each language! Players see holograms in their client language.

Supported locales: `en_US`, `ru_RU`, `zh_CN`, `de_DE`, `fr_FR`, `es_ES`, `pt_BR`, `ja_JP`, `ko_KR`, `uk_UA`

### 🧩 PlaceholderAPI
Use any PlaceholderAPI placeholders in your holograms:
- `%player_name%` — Player's name
- `%player_level%` — Player's level
- `%server_online%` — Online players count
- And many more from other plugins!

### ⚙️ Configuration
```json
{
  "updateInterval": 20,
  "defaultLocale": "en_US",
  "defaultLineSpacing": 0.25,
  "maxHologramDistance": 32,
  "supportedLocales": ["en_US", "ru_RU", ...]
}
```

| Option | Description |
|--------|-------------|
| `updateInterval` | Default update interval in seconds. |
| `defaultLocale` | Fallback language. |
| `defaultLineSpacing` | Space between lines. |
| `maxHologramDistance` | Max distance to find nearest hologram. |

### 📦 Installation
1. Download the latest release.
2. Place the `.jar` file in your `plugins` folder.
3. Restart the server.
4. Use `/holo create` to start!

### 🔗 Dependencies
- **Required**: Allay Server
- **Optional**: PlaceholderAPI (for dynamic placeholders)

---

<div align="center">
    <br>
    <p>Created by <b>Miroshka</b> specifically for <b>Allay</b> with ❤️</p>
</div>
