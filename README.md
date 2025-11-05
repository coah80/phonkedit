# Phonk Edit

yes i used ai for this readme

Phonk Edit is a Fabric mod that adds short "phonk edit" audio/visual effects (skulls, freeze-frame, shake, and short phonk tracks) to Minecraft gameplay.

Quick reference — everything an end user needs to install, run, build, and report issues.

Requirements
- Minecraft 1.21.1
- Fabric Loader (compatible with your Minecraft version)
- Fabric API
- Java 17+ (Java 21 recommended)
- Optional for in-game config UI: Mod Menu and Cloth Config

Install
1. Download the latest phonkedit-*.jar from Releases.
2. Copy the JAR into your .minecraft/mods/ folder.
3. Launch Minecraft with the Fabric profile.

Optional (recommended)
- Install Mod Menu and Cloth Config for an in-game configuration screen:
  - In-game: Main Menu → Mods → Phonk Edit → Configure

Basic usage
- Effects can trigger on actions (placing/breaking blocks, hitting/taking damage, low health, being airborne).
- Configure Trigger Chance, effect duration, skull size, and audio mixing from the in-game config or the config file.

Custom content
- Custom images: place PNG/JPG/GIF in .minecraft/config/phonkedit/images/ (auto-resized to 256×256).
- Custom songs: place .ogg files in .minecraft/config/phonkedit/songs/ (enable the generated resource pack to use them in-game).

Build from source (quick guide)
Prerequisites
- Java JDK 21 recommended (JDK 17+ supported)
- Git
- No system Gradle required — the Gradle wrapper is included.

Clone and build (Windows)
```powershell
git clone https://github.com/coah80/phonkedit.git
cd phonkedit
# Build the mod JAR
.\gradlew.bat build
# Run a development client
.\gradlew.bat runClient
```

Clone and build (macOS / Linux)
```bash
git clone https://github.com/coah80/phonkedit.git
cd phonkedit
# Make the wrapper executable if needed
chmod +x gradlew
# Build the mod JAR
./gradlew build
# Run a development client
./gradlew runClient
```

Outputs
- Built JAR(s) appear in build/libs/
- Use the generated JAR as you would a release JAR (drop into .minecraft/mods/)

Developer tips
- If the build can't find Java, set JAVA_HOME to your JDK install and ensure java -version reports a supported JDK.
- For IDEs: import the project as a Gradle project (the wrapper will handle toolchains).
- If you hit dependency or environment errors, try a clean build: ./gradlew clean build
- Multi-version builds: use the Stonecutter CLI (`stonecutter active <mc-version>`) before running Gradle tasks if you need to switch off the default (1.21.1).

If something goes wrong
- Ensure Fabric API and the right Java version are installed.
- Check the game log for errors (logs/latest.log).
- Try removing other mods to check for conflicts.

Reporting issues
- Use the issue templates (Bug, Feature, Question) and include: Phonk Edit version, Minecraft version, Fabric Loader, Fabric API, Java version, steps to reproduce, and logs/screenshots when appropriate.

License
- MIT — see LICENSE file in this repository.

Thanks for using Phonk Edit — enjoy the effects!
