# 🧭 Development Plan

**Project:** Iris Keyboard Pistolwhip'd  
**Baseline:** Upstream Iris Keyboard 6.5.3 / versionCode 154  
**Target first fork release:** 6.6.0 / versionCode 155  
**Last updated:** 2026-11-22

## Purpose

Add Microsoft SwiftKey-like word suggestions, conservative automatic correction, and a locally learned personal vocabulary to the existing Iris keyboard while preserving its current toolbar, clipboard, AI tools, AMOLED theme, and layout controls.

## Requirements

- Install as a separate Android app beside upstream Iris.
- System-visible name: **Iris Keyboard Pistolwhip'd**.
- Distinct application ID from upstream: `pro.pistolwhip.iris.keyboard`.
- Candidate strip sits immediately below the existing utility toolbar and above the keyboard keys.
- Suggestions update as the user types; tapping a candidate replaces the current word.
- Likely misspellings are corrected when Space is pressed.
- After automatic correction, Backspace once removes the inserted space; the second Backspace deletes the final character of the corrected word without restoring the original misspelling.
- Repeatedly used words and word pairs are learned locally; custom words can be entered manually.
- Do not route ordinary autocorrect through AI Copilot, OpenRouter, or another network API.
- Suppress suggestions in password, email, URL, and explicitly restricted fields.
- Preserve upstream source attribution and Apache-2.0 notices.

## Phases and readiness gates

### Phase 1 — Fork identity and reproducible build

**Status:** CI identity checks passed; physical side-by-side installation remains unverified.

**Deliverables**
- Distinct application ID, app label, and provider authority.
- Derivative-owned release documentation and GitHub Actions build.
- No inherited upstream release/update path that can install upstream package builds.

**Readiness gate**
- CI builds the NoMlKit variant.
- APK package ID and app label are inspected from the built artifact.
- Side-by-side installation is confirmed on a physical Android device.

### Phase 2 — Typing engine and candidate strip

**Status:** Implementation builds; automated coverage includes the two-press autocorrect Backspace sequence. Real-device typing acceptance remains unverified.

**Deliverables**
- Candidate strip between utility toolbar and keys.
- Android spell-checker-backed candidate requests.
- Conservative auto-correction on Space.
- Candidate tap-to-replace behavior.
- Local repeated-word learning, custom words, and bigram-based next-word predictions.
- Input-type protections.

**Readiness gate**
- Automated build/tests pass.
- Manual test matrix covers candidate display, manual replacement, automatic correction, custom words, learned words, next-word predictions, protected fields, and backspace sequence.

### Phase 3 — Release and device acceptance

**Status:** v6.6.0 is published; physical-device acceptance remains pending.

**Deliverables**
- Versioned APK release workflow and release notes.
- Updated README and known-limitations record.
- Verified release asset and SHA-256.

**Readiness gate**
- Workflow completed; APK build succeeded; automated tests reported separately.
- Release tag points to the exact source commit used for the build.
- Physical-device install and typing behavior are explicitly marked verified or unverified.

## Current implementation status

- **Original source commit:** `057a1c68d2c0f58c044c2d64ef75fe8cb139e0be` (v6.6.0); v6.6.1 source commit is recorded in its release notes.
- **Latest release:** [v6.6.1](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/releases/tag/v6.6.1) (prior release: [v6.6.0](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/releases/tag/v6.6.0))
- **Tag:** `v6.6.0` points to the exact release source commit.
- **Build/tests:** NoMlKit debug APK built successfully; 8 JUnit tests passed with zero failures/errors.
- **Identity:** CI verified package `pro.pistolwhip.iris.keyboard`, label `Iris Keyboard Pistolwhip'd`, and provider authority `pro.pistolwhip.iris.keyboard.gifprovider`.
- **Artifact:** APK and SHA-256 were retrieved from the successful release workflow artifact; checksum verification passed.
- **Not yet verified:** Physical installation beside upstream Iris, keyboard selection, live suggestions/corrections, custom vocabulary behavior, and the two-press backspace sequence on target hardware.
