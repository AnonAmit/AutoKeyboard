# AutoKeyboard 🚀

**Privacy-first, AI-powered Android keyboard that rewrites any text in any app with one tap.**

![Android](https://img.shields.io/badge/Android-26%2B-green) ![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue) ![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.12-purple)

---

## What is AutoKeyboard?

AutoKeyboard sits inside your Android keyboard and rewrites your messages in any tone: Professional, Flirty, Poetic, Gen-Z, Shakespearean, or your own custom style. Works in WhatsApp, Gmail, Instagram, and every other app.

### Key Features
- **9 Tone Presets** — Professional, Friendly, Casual, Flirty, Witty, Poetic, Gen-Z, Formal, Custom
- **Custom Prompts** — "Talk like my GF" or "Make it Shakespearean"
- **10 Cloud Providers** — OpenAI, Gemini, Claude, Mistral, Groq, Cohere, OpenRouter, DeepSeek, Together AI, Custom
- **5 On-Device Models** — Gemma, Phi-3, Qwen, Llama 3.2, MLC (fully offline)
- **Fallback Chain** — Automatic failover across configured providers
- **Zero Message Storage** — No text is ever stored or logged
- **Encrypted API Keys** — AndroidKeyStore + EncryptedSharedPreferences

---

## Architecture

```
MVVM + Clean Architecture + Hilt DI
├── domain/        # AIProvider interface (contract)
├── data/
│   ├── model/     # Tone, ProviderConfig, RewriteResult
│   ├── local/     # DataStore preferences, SecureStorage
│   └── provider/
│       ├── cloud/ # 10 cloud providers (OpenAI, Gemini, Claude...)
│       └── local/ # 5 on-device providers (Gemma, Phi-3, Qwen...)
├── service/       # KeyboardService (IME), KeyboardViewModel
├── ui/
│   ├── theme/     # Neon Nocturne design system (colors, shapes, typography)
│   ├── keyboard/  # KeyboardScreen, KeyTile, EmojiStrip
│   ├── sheets/    # ToneSelectorSheet, RewriteResultsSheet
│   ├── settings/  # SettingsScreen, SettingsViewModel
│   ├── onboarding/# OnboardingScreen
│   ├── components/# ShimmerEffect, GradientButton
│   └── navigation/# NavGraph
└── di/            # Hilt modules
```

---

## Setup

### Prerequisites
- Android Studio Ladybug (2024.2) or newer
- Android SDK 35
- JDK 17+

### Build
```bash
git clone https://github.com/AnonAmit/AutoKeyboard.git
cd AutoKeyboard
./gradlew assembleDebug
```

### Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Then go to **Settings → System → Languages & Input → On-screen keyboard → Manage on-screen keyboards** and enable **AutoKeyboard**.

---

## Configure AI Providers

1. Open AutoKeyboard app from launcher
2. Go to **Settings → AI Providers**
3. Tap a provider card and enter your API key
4. The key is encrypted and stored in AndroidKeyStore

### Provider API Key Sources
| Provider | Get API Key |
|----------|------------|
| OpenAI | [platform.openai.com/api-keys](https://platform.openai.com/api-keys) |
| Gemini | [aistudio.google.com](https://aistudio.google.com/app/apikey) |
| Claude | [console.anthropic.com](https://console.anthropic.com/account/keys) |
| Groq | [console.groq.com](https://console.groq.com/keys) |
| OpenRouter | [openrouter.ai/keys](https://openrouter.ai/keys) |

---

## Privacy

- **Zero message storage** — Input text exists only in RAM during the rewrite call
- **No analytics by default** — Optional anonymous stats (opt-in)
- **Encrypted secrets** — API keys stored via `EncryptedSharedPreferences` + `AndroidKeyStore`
- **No cloud dependency** — Can run fully offline with on-device models
- **Open source** — Full code audit possible

---

## Design System: Neon Nocturne

| Token | Value | Usage |
|-------|-------|-------|
| Background | `#0C0C1F` | OLED-safe base |
| Primary | `#B6A0FF` | Purple accent |
| Secondary | `#4AF8E3` | Teal accent |
| Key Tiles | `#2D2D44` | Keyboard keys |
| On-Surface | `#E5E3FF` | Primary text |

Typography: Plus Jakarta Sans (headlines) + Inter (body)

---

## License

MIT License — see [LICENSE](LICENSE) for details.
