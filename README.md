# Phonk Edit

A Fabric Minecraft mod that brings YouTube Shorts style phonk edit effects to your gameplay.

## Features

- 🎵 **Phonk Audio System** - Random phonk tracks play during effects
- 💀 **Freeze Overlay** - Fullscreen greyscale effect with animated skull graphics
- 🎮 **Custom Rendering** - Powered by the Renderer library for smooth visuals
 
- ⚡ **Performance Optimized** - Lightweight and efficient

## Quickstart Tutorial

1) Install the mod
- Install Fabric Loader for Minecraft 1.21.1 and drop Fabric API + this mod JAR into `.minecraft/mods/`

2) Configure in-game (recommended)
- With Mod Menu + Cloth Config installed, open Mods → Phonk Edit and set:
  - Trigger Chance to 0.50 (50%) or your preferred value
  - Skull Size (scale) to 0.4 for the look in screenshots (or tweak to taste)

3) Use it
- Trigger it by placing/breaking blocks, hitting entities, taking damage, low health, or staying airborne ≥ 1.3s
- The effect won’t stack and has a small cooldown right after joining a world

4) Custom images
- Put PNGs or GIFs in `.minecraft/config/phonkedit/images/` (defaults export on first run)
- `skull20.png` shows during the PHONK6 track if present

5) Custom songs (optional)
- Put `.ogg` files in `.minecraft/config/phonkedit/songs/`
- Enable the generated pack at `.minecraft/resourcepacks/phonkedit-custom-songs/` once in Resource Packs
- Your songs are included in the random rotation

6) Saving/quitting
- Opening the pause menu ends the effect so you can save/quit reliably

## Installation

### Requirements

- **Minecraft** 1.21.1
- **Fabric Loader** 0.15.0 or higher
- **Fabric API** (latest version)
- **Java** 21 recommended (17+ supported)
- For in-game config UI: **Mod Menu** and **Cloth Config**

### Steps

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your `mods` folder
3. Download the latest **Phonk Edit** release from [here](https://github.com/coah80/phonkedit/releases)
4. Place the `phonkedit-*.jar` file in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Configuration

You can configure Phonk Edit in two ways:

### In-Game (Recommended)
1. Install [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config)
2. Click "Mods" button in main menu
3. Find "Phonk Edit" and click the config button
4. Adjust settings with sliders and toggles
5. Changes save automatically!

### Manual Configuration
Configuration file is located at `.minecraft/config/phonkedit.json`

```json
{
  "enablePhonkEffect": true,
  "triggerChance": 0.50,
  "effectDuration": 3000,
  "shakeIntensity": 1.0
}
```

- `enablePhonkEffect` - Enable/disable the mod (default: `true`)
- `triggerChance` - Chance of effect triggering (0.0-1.0, default: `0.50`)
- `effectDuration` - Effect duration in milliseconds (default: `3000`)
- `shakeIntensity` - Skull shake intensity multiplier (default: `1.0`, higher = more shake, try `5.0` for outrageous!)
 

## Building from Source

### Prerequisites

- **Java Development Kit (JDK) 21** (recommended; 17+ supported)
- **Git**
 - You do not need Gradle installed (the Gradle wrapper is included)

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

Tips
- If the build can’t find Java on Windows, install a 64‑bit JDK 21 and ensure `java -version` works in PowerShell
- If you have multiple JDKs, set `JAVA_HOME` before running Gradle
- In VS Code, you can also use the “Run Minecraft Client” task if it’s available

## Troubleshooting

### Mod doesn't load
- Verify you have Fabric Loader and Fabric API installed
- Check that you're using Minecraft 1.21.1
- Ensure Java 21 (or at least 17) is installed

### No sound from custom songs
- Enable the generated resource pack at `.minecraft/resourcepacks/phonkedit-custom-songs/`
- Place `.ogg` files in `.minecraft/config/phonkedit/songs/` (simple lowercase filenames recommended)
- Use the Resource Packs menu “Done” button to reload after changes

### Can’t save while the effect is active
- The effect ends when you open the pause menu, specifically so saving and quitting always works

## Support

- **Issues & Bugs:** [GitHub Issues](https://github.com/coah80/phonkedit/issues)
- **Feature Requests:** [GitHub Issues](https://github.com/coah80/phonkedit/issues)

## License

MIT License - See [LICENSE](LICENSE) file for details

## Credits

- Built with [Fabric](https://fabricmc.net/)
- Powered by [Renderer](https://github.com/0x150/renderer) by 0x150

## Custom Songs (Optional)

Drop-in via config folder:

1) Put .ogg files in `.minecraft/config/phonkedit/songs/`
2) On first run, the mod exports the built-in tracks there so you can delete/replace them
3) The mod generates a resource pack at `.minecraft/resourcepacks/phonkedit-custom-songs/`
4) Enable “Phonk Edit - Custom Songs” in the Resource Packs menu (one time)
5) After that, changes in the songs folder are picked up on reload

Notes
- Use simple lowercase filenames (letters, numbers, underscores). We sanitize names automatically.
- Tracks are streamed and included in the same random selection as built-ins.
- Please trim your tracks to the beat drop, itll work best then.
- Custom songs are capped at 5 seconds, to minimize the jank

## Custom Images (Optional aswell)

Drop-in via config folder:

1) Put images in `.minecraft/config/phonkedit/images/`
  - Supported: PNG, JPG/JPEG, GIF (including animated), BMP, WBMP, TIF/TIFF
2) On first run, the mod exports the built-in skulls there so you can delete/replace them
3) Images are auto-resized to 256×256
5) Restart Minecraft to reload images (they load at game start)

Tips
- For best results, start from 256×256 PNGs with transparent background
- Animated GIFs will play; total duration depends on the frame delays inside the GIF
