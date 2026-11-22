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
- Backspace once removes the inserted space; the next backspace deletes the last character of the corrected word.
- Repeatedly used words and word pairs are learned locally; custom words can be entered manually.
- Do not route ordinary autocorrect through AI Copilot, OpenRouter, or another network API.
- Suppress suggestions in password, email, URL, and explicitly restricted fields.
- Preserve upstream source attribution and Apache-2.0 notices.

## Phases and readiness gates

### Phase 1 — Fork identity and reproducible build

**Deliverables**
- Distinct application ID, app label, and provider authority.
- Derivative-owned release documentation and GitHub Actions build.
- No inherited upstream release/update path that can install upstream package builds.

**Readiness gate**
- CI builds the NoMlKit variant.
- APK package ID and app label are inspected from the built artifact.
- Side-by-side installation is confirmed on a physical Android device.

### Phase 2 — Typing engine and candidate strip

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

**Deliverables**
- Versioned APK release workflow and release notes.
- Updated README and known-limitations record.
- Verified release asset and SHA-256.

**Readiness gate**
- Workflow completed; APK build succeeded; automated tests reported separately.
- Release tag points to the exact source commit used for the build.
- Physical-device install and typing behavior are explicitly marked verified or unverified.

## Current implementation status

The branch contains the first implementation of the candidate strip, Android spell-checker integration, local learning, custom vocabulary preference, app identity separation, and release/CI setup. No APK is considered available until GitHub Actions has built it successfully and the release asset has been verified. Physical-device behavior remains unverified until installed and exercised on target hardware.
