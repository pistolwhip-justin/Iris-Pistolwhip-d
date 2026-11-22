# ⌨️ Iris Keyboard Pistolwhip'd

![Project](https://img.shields.io/badge/Android-Keyboard-3DDC84?style=flat-square&logo=android&logoColor=white) ![Release](https://img.shields.io/github/v/release/pistolwhip-justin/Iris-Pistolwhip-d?style=flat-square) ![License](https://img.shields.io/github/license/pistolwhip-justin/Iris-Pistolwhip-d?style=flat-square) ![Maintained](https://img.shields.io/badge/maintained-yes-green?style=flat-square)

**Iris Keyboard Pistolwhip'd** is my working fork of [Iris Keyboard](https://github.com/MohamadOday/Iris), itself based on AOSP / [Simple Keyboard](https://github.com/rkkr/simple-keyboard). It keeps the Iris features worth keeping and adds the typing behavior I expect from a modern keyboard—without replacing the whole project with a different app.

The immediate mission: make autocorrect actually behave like a keyboard should. Suggestions while typing, sensible automatic corrections, and a vocabulary that learns the words I actually use.

> This is an independent community fork. It is not an official release of Iris Keyboard and is not endorsed by the upstream author.

---

## 📦 Current release — v6.6.0

**Status:** Published

- [GitHub Release page](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/releases/tag/v6.6.0)
- [Direct APK download](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/releases/download/v6.6.0/Iris-Keyboard-Pistolwhipd-v6.6.0-NoMlKit.apk)
- [SHA-256 checksum file](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/releases/download/v6.6.0/Iris-Keyboard-Pistolwhipd-v6.6.0-NoMlKit.apk.sha256)

**Package:** `pro.pistolwhip.iris.keyboard`  
**Version code:** `155`  
**SHA-256:** `1c82ed5e9f286cfdb4f57f1d9ca8d106687e3617feb89c12e6a9774e83bbaac0`

This is a debug-signed sideload build. CI verified the distinct package ID, visible app label, and GIF provider authority. The APK and checksum were downloaded from the release workflow artifact and the checksum passed local verification. Physical-device installation and typing behavior still need a real-device test.

---

## 🎯 Why this fork exists

Iris already brings a useful foundation: clipboard history, an optional AI toolbar, theming, layout controls, and a compact keyboard. The missing piece for my daily use was a proper typing engine. This fork adds that without turning ordinary typing into an AI/API call.

## ⚡ Features

### ✍️ SwiftKey-style typing

- **Live candidate strip** directly between the existing Iris utility toolbar and the keyboard keys.
- **Automatic correction** of likely misspellings when Space is pressed. Corrections are conservative: the Android spell-checker must flag a likely typo, and the proposed word must be close to the typed word.
- **Tap-to-replace** candidates while typing.
- **Next-word suggestions** based on locally learned word pairs.
- **Automatic vocabulary learning** after repeated use, plus a custom-word list you can maintain yourself.
- **Natural backspace behavior:** after an automatic correction inserts a space, Backspace once removes only that space; Backspace again removes the last letter of the corrected word. No separate undo control.

### 🧰 Iris features retained

- Clipboard history, pinning, search, and the existing clipboard toolbar.
- AI Copilot and custom OpenAI-compatible endpoints.
- AMOLED true-black theme and Material You color support.
- Keyboard height, spacing, key sizing, and layout customization.
- Translation, GIF/media tools, emoji controls, and mechanical sound options.

The autocorrect path is separate from AI Copilot. It does not send typing to OpenRouter or another AI API.

## 🧠 Vocabulary, privacy, and spell-checking

- Your custom words, learned words, and word-pair frequencies are stored in the fork's local Android preferences.
- Custom words are managed in **Settings → Preferences → Autocorrect & Learning → My custom words**. Separate words with commas or line breaks.
- Repeatedly used words are learned on-device. Word-pair counts are used for next-word predictions.
- Base spelling candidates come from Android's currently selected spell-checker service through the Android text-services API. The fork itself does not make a network request for autocorrect, but the selected spell-checker is a separate system component and its own privacy/network behavior depends on the device configuration. Choose an on-device spell-checker if you want the entire typing path to remain offline.
- Suggestions and corrections are suppressed for password, email, URL, and fields that explicitly disable suggestions.

## 📲 Install alongside the original Iris

This fork intentionally uses a different Android application ID:

`pro.pistolwhip.iris.keyboard`

The upstream Iris package remains `nabu.iris.keyboard`. Android therefore treats this fork as a separate app: **you do not need to uninstall the original Iris**.

The system-visible app name is **Iris Keyboard Pistolwhip'd**, so it is distinguishable in the installer, Settings, launcher, and keyboard list.

1. Download the APK from [GitHub Releases](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/releases).
2. Install it normally; it should install beside the original Iris.
3. Open Android's keyboard settings, enable **Iris Keyboard Pistolwhip'd**, and select it as the active keyboard.
4. Open the fork's settings and configure **Autocorrect & Learning**.

The v6.6.0 release is a debug-signed sideload build. It is intended for installation/testing, not a Play Store or production-signed release. A stable release-signing key will be needed if future APKs must update this installation without reinstalling.

## 🛠️ Build from source

### Requirements

- JDK 17
- Android SDK / Build Tools compatible with compile SDK 36
- The repository's Gradle wrapper

### Build and test

```bash
git clone https://github.com/pistolwhip-justin/Iris-Pistolwhip-d.git
cd Iris-Pistolwhip-d
./gradlew --no-daemon testNomlkitDebugUnitTest assembleNomlkitDebug
```

The NoMlKit variant avoids the optional proprietary ML Kit translation dependency. The APK is generated under:

`app/build/outputs/apk/nomlkit/debug/`

The version-specific GitHub Actions workflow creates a version tag on the exact `main` commit, builds the APK, and publishes immutable GitHub Release assets. v6.6.0 is published; future releases use their own version-specific workflow and tag. Release workflows and APK filenames are version-specific; prior releases are not overwritten.

## 🧭 Project structure

```
app/
  src/main/java/nabu/iris/keyboard/
    latin/
      AutocorrectBarController.java
      LatinIME.java
      RichInputConnection.java
      inputlogic/InputLogic.java
  src/main/res/
    layout/input_view.xml
    xml/prefs_screen_preferences.xml
  src/test/java/nabu/iris/keyboard/latin/
    AutocorrectBarControllerTest.java
.github/workflows/
  ci.yml
  release-6.6.0.yml
docs/
  DEVELOPMENT_PLAN.md
  DECISIONS.md
```

## 🤝 Upstream, licensing, and contributions

- Upstream Iris Keyboard: [MohamadOday/Iris](https://github.com/MohamadOday/Iris)
- Original lightweight keyboard foundation: [rkkr/simple-keyboard](https://github.com/rkkr/simple-keyboard)
- This fork's source and releases: [pistolwhip-justin/Iris-Pistolwhip-d](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d)

The project retains the upstream Apache License 2.0, copyright notices, and third-party acknowledgments. Fork-specific changes do not imply upstream authorship or endorsement.

Bug reports and fork-specific improvements belong in this repository's [Issues](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/issues).

## ⚠️ Known limitations

- Spell-check candidates depend on an enabled Android spell-checker that supports the current language.
- The first implementation uses conservative word-level corrections on Space; it does not attempt gesture decoding or sentence rewriting.
- Learned vocabulary and prediction frequencies are local to this fork's app data. They are not automatically imported from the original Iris installation.
- Physical-device verification is still required for the install confirmation, launcher label, keyboard selection, and real typing behavior.

---

Last updated: 2026-11-22
