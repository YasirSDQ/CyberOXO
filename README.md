# 🎮 CyberOXO v2.5

<div align="center">

![Version](https://img.shields.io/badge/version-2.5-brightgreen)
![Platform](https://img.shields.io/badge/platform-Android-blue)
![Kotlin](https://img.shields.io/badge/language-Kotlin-purple)
![License](https://img.shields.io/badge/license-MIT-orange)

**The Ultimate Cyberpunk Tic-Tac-Toe Experience**

*Modern Design • Multiple Modes • Adaptive AI • 5 Unique Themes*

![CyberOXO Banner](https://via.placeholder.com/800x400/0f0f1f/00ffff?text=CyberOXO+v2.5+-+Future+of+Tic-Tac-Toe)

</div>

---

## ✨ Features

### 🎨 5 Unique Cyberpunk Themes
Choose your aesthetic from our curated collection:
- **Neon Cyber** - Classic pink & cyan neon glow
- **Matrix Code** - Green hacker terminal vibes
- **Sunset Wave** - Warm orange & teal gradients
- **Ice Storm** - Cool blue & purple frost effects
- **Golden Elite** - Premium gold luxury finish

### 🎯 Advanced Game Modes
- **Dynamic Board Sizes**: Switch between 3×3, 4×4, and 5×5 grids
- **Smart Win Conditions**: Auto-adjusted winning line requirements
- **Difficulty Levels**: 
  - 🟢 **Easy** - Perfect for beginners
  - 🟡 **Medium** - Balanced challenge
  - 🔴 **Impossible** - Unbeatable AI using Minimax algorithm

### 📊 Player Statistics
Track your gaming prowess:
- Total games played
- Wins / Losses / Draws
- Win rate percentage
- Performance analytics

### 🤖 Enhanced AI Engine
- Adaptive difficulty based on board size
- Smart move prediction
- Real-time decision making
- No lag, instant responses

### 💫 Modern UI/UX
- Glassmorphism design elements
- Smooth 60fps animations
- Haptic feedback support
- Sound effects integration
- Responsive bottom sheet dialogs
- Dark mode optimized

---

## 🚀 Quick Start

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 24+ (Android 7.0 Nougat)
- Kotlin 1.9+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/cyberoxo.git
   cd cyberoxo
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync dependencies
   - Wait for indexing to complete

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the ▶️ Run button
   - Enjoy CyberOXO!

### Build from Command Line
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

---

## 📱 Screenshots

<div align="center">

| Theme Selector | Game Board | Statistics |
|:--------------:|:----------:|:----------:|
| ![Theme](https://via.placeholder.com/200x400/1a1a2e/00ffff?text=Themes) | ![Game](https://via.placeholder.com/200x400/1a1a2e/ff00ff?text=Gameplay) | ![Stats](https://via.placeholder.com/200x400/1a1a2e/00ff00?text=Statistics) |

</div>

---

## 🏗️ Project Structure

```
app/
├── src/main/
│   ├── java/cyberoxo/io/
│   │   ├── ui/
│   │   │   ├── ModeSelectionActivity.kt    # Main dashboard
│   │   │   ├── GameActivity.kt             # Game board logic
│   │   │   └── adapters/                   # RecyclerView adapters
│   │   ├── model/
│   │   │   └── GameModels.kt               # Data classes & enums
│   │   └── engine/
│   │       ├── GameEngine.kt               # Core game logic
│   │       └── BotEngine.kt                # AI implementation
│   ├── res/
│   │   ├── layout/                         # XML layouts
│   │   ├── values/                         # Colors, strings, themes
│   │   └── drawable/                       # Assets & icons
│   └── AndroidManifest.xml
├── build.gradle.kts                        # App-level build config
└── proguard-rules.pro                      # Release optimization
```

---

## ⚙️ Technical Specifications

### Tech Stack
- **Language**: Kotlin 1.9+
- **UI**: XML Layouts + Material Design Components
- **Architecture**: MVC Pattern
- **Storage**: SharedPreferences for settings persistence
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Key Dependencies
```kotlin
// Material Design
implementation("com.google.android.material:material:1.11.0")

// AndroidX Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")

// Constraint Layout
implementation("androidx.constraintlayout:constraintlayout:2.1.4")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

---

## 🎮 How to Play

1. **Select Theme**: Tap the theme chip to choose your visual style
2. **Choose Board Size**: Pick 3×3, 4×4, or 5×5 grid
3. **Set Difficulty**: Select Easy, Medium, or Impossible
4. **Start Game**: Tap "Play Now" to begin
5. **Make Moves**: Tap cells to place your mark (X)
6. **Win Condition**: Get required number in a row (horizontal, vertical, or diagonal)

### Win Requirements
| Board Size | Marks Needed |
|------------|--------------|
| 3×3        | 3            |
| 4×4        | 4            |
| 5×5        | 5            |

---

## 🛠️ Configuration

### Customization Options

Edit `GameSettings` in `GameModels.kt`:
```kotlin
data class GameSettings(
    val boardSize: BoardSize = BoardSize.SIZE_3X3,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val theme: GameTheme = GameTheme.NEON_CYBER,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)
```

### Adding New Themes

1. Add theme to `GameTheme` enum in `GameModels.kt`
2. Define colors in `res/values/colors.xml`
3. Update theme selector adapter in `ModeSelectionActivity.kt`

---

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- ✅ Game logic validation
- ✅ Win condition detection
- ✅ AI move generation
- ✅ Settings persistence
- ✅ UI component rendering

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 CyberOXO Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Contribution Guidelines
- Follow Kotlin coding conventions
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR

---

## 🐛 Bug Reports & Feature Requests

Found a bug or have a feature request? Please open an issue on our [GitHub Issues](https://github.com/yourusername/cyberoxo/issues) page.

**When reporting bugs, include:**
- Device model and Android version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots or screen recordings (if applicable)

---

## 📞 Support

Need help? Contact us:
- 📧 Email: support@cyberoxo.io
- 💬 Discord: [Join our server](https://discord.gg/cyberoxo)
- 🐦 Twitter: [@CyberOXO_Game](https://twitter.com/CyberOXO_Game)

---

## 🙏 Acknowledgments

- Thanks to the Kotlin community for amazing tools
- Material Design guidelines by Google
- All contributors who made CyberOXO possible
- Special thanks to our beta testers

---

<div align="center">

### Made with ❤️ by the CyberOXO Team

**Version 2.5** | Last Updated: 2024

[⬆ Back to Top](#-cyberoxo-v25)

</div>
