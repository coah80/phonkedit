# Phonk Edit

A Fabric Minecraft mod that brings YouTube Shorts style phonk edit effects to your gameplay.

## Features

- 🎵 **Phonk Audio System** - Random phonk tracks play during effects
- 💀 **Freeze Overlay** - Fullscreen greyscale effect with animated skull graphics
- 🎮 **Custom Rendering** - Powered by the Renderer library for smooth visuals
- ⚡ **Performance Optimized** - Lightweight and efficient

## Installation

### Requirements

- **Minecraft** 1.21.1
- **Fabric Loader** 0.15.0 or higher
- **Fabric API** (latest version)
- **Java** 17 or higher

### Steps

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your `mods` folder
3. Download the latest **Phonk Edit** release from [here](https://github.com/coah80/phonkedit/releases)
4. Place the `phonkedit-*.jar` file in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Configuration

You can configure Phonk Edit in two ways:

### In-Game (Recommended)
1. Install [Mod Menu](https://modrinth.com/mod/modmenu)
2. Click "Mods" button in main menu
3. Find "Phonk Edit" and click the config button
4. Adjust settings with sliders and toggles
5. Changes save automatically!

### Manual Configuration
Configuration file is located at `.minecraft/config/phonkedit.json`

```json
{
  "enablePhonkEffect": true,
  "triggerChance": 0.10,
  "effectDuration": 3000,
  "shakeIntensity": 1.0
}
```

- `enablePhonkEffect` - Enable/disable the mod (default: `true`)
- `triggerChance` - Chance of effect triggering (0.0-1.0, default: `0.10`)
- `effectDuration` - Effect duration in milliseconds (default: `3000`)
- `shakeIntensity` - Skull shake intensity multiplier (default: `1.0`, higher = more shake, try `5.0` for outrageous!)

## Building from Source

### Prerequisites

- **Java Development Kit (JDK) 21**
- **Git**

### Windows

```powershell
git clone https://github.com/coah80/phonkedit.git
cd phonkedit
.\gradlew.bat build
```

### Linux/macOS

```bash
git clone https://github.com/coah80/phonkedit.git
cd phonkedit
./gradlew build
```

The compiled JAR will be in `build/libs/`

### Development

To run the mod in a development environment:

```powershell
# Windows
.\gradlew.bat runClient

# Linux/macOS
./gradlew runClient
```

## Troubleshooting

### Mod doesn't load
- Verify you have Fabric Loader and Fabric API installed
- Check that you're using Minecraft 1.21.1
- Ensure Java 17+ is installed

## Support

- **Issues & Bugs:** [GitHub Issues](https://github.com/coah80/phonkedit/issues)
- **Feature Requests:** [GitHub Issues](https://github.com/coah80/phonkedit/issues)

## License

MIT License - See [LICENSE](LICENSE) file for details

## Credits

- Built with [Fabric](https://fabricmc.net/)
- Powered by [Renderer](https://github.com/0x150/renderer) by 0x150
