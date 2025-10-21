# Phonk Edit

A Fabric Minecraft 1.21.1 mod that adds YouTube Shorts style phonk edit effects to your gameplay using the Renderer library.

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

1. Download the latest release from the [Releases](https://github.com/coah80/phonkedit/releases) page
2. Place the mod JAR file in your `.minecraft/mods` folder
3. Launch Minecraft with Fabric

## Building from Source

### Prerequisites

- Java 21 (JDK)
- Git

### Build Steps

```bash
# Clone the repository
git clone https://github.com/coah80/phonkedit.git
cd phonkedit

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

## Contributing

Thank you for considering contributing! Here's how you can help:

### Reporting Bugs

Before creating bug reports, check the [issue tracker](https://github.com/coah80/phonkedit/issues) to see if it's already reported.

Include in your bug report:
- Clear description and steps to reproduce
- Expected vs actual behavior
- Screenshots/videos if applicable
- Minecraft version, mod version, and other mods installed
- Crash reports or logs if applicable

### Suggesting Features

Feature requests are welcome! Include:
- Clear description of the feature
- Use cases and benefits
- Any relevant examples or mockups

### Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Test thoroughly in Minecraft
5. Commit with clear messages (`git commit -m 'Add amazing feature'`)
6. Push to your branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

**Guidelines:**
- Follow existing code style
- Comment complex logic
- Update documentation as needed
- Test in Minecraft client
- Include screenshots/videos for visual changes

## Creating a Release

To create a new release:

```bash
# Commit your changes
git add .
git commit -m "Release v2.0.0"

# Create and push tag
git tag v2.0.0
git push origin main
git push origin v2.0.0
```

GitHub Actions will automatically build and create the release with JAR files attached.

## License

This project is licensed under the MIT License.

## Credits

- Built with [Fabric](https://fabricmc.net/)
- Uses [Renderer library](https://github.com/0x150/renderer) by 0x150
