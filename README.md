# CursedPhonk Renderer Edition

A Fabric Minecraft 1.20.1 mod that adds YouTube Shorts style phonk edit effects to your gameplay using the Renderer library.

## Features

- 🎵 Phonk audio system
- 💀 Fullscreen greyscale freeze overlay with skull graphics
- 🎮 Custom rendering effects using Renderer library

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.15.0+
- Fabric API
- Java 17 or higher

## Installation

1. Download the latest release from the [Releases](https://github.com/cursedphonk/cursedphonk-renderer/releases) page
2. Place the mod JAR file in your `.minecraft/mods` folder
3. Launch Minecraft with Fabric

## Building from Source

### Prerequisites

- Java 21 (JDK)
- Git

### Build Steps

```bash
# Clone the repository
git clone https://github.com/cursedphonk/cursedphonk-renderer.git
cd cursedphonk-renderer

# Build the mod (Windows)
gradlew.bat build

# Build the mod (Linux/Mac)
./gradlew build
```

The compiled JAR will be in `build/libs/`

## Development

### Running the Client

```bash
# Windows
gradlew.bat runClient

# Linux/Mac
./gradlew runClient
```

## License

This project is licensed under the MIT License.

## Issues & Support

Found a bug or have a feature request? Please open an issue on our [Issue Tracker](https://github.com/cursedphonk/cursedphonk-renderer/issues).

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Credits

- Built with [Fabric](https://fabricmc.net/)
- Uses [Renderer library](https://github.com/0x150/renderer) by 0x150
