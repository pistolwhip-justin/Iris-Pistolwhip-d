# 🧭 Development Plan

**Project:** Iris Keyboard Pistolwhip'd  
**Baseline:** Upstream Iris Keyboard 6.5.3 / versionCode 154  
**Target first fork release:** 6.6.0 / versionCode 155  
**Last updated:** 2026-11-27

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

**Status:** v6.6.1 is published; physical-device acceptance remains pending.

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
- **Latest prior release:** [v6.6.1](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/releases/tag/v6.6.1) (prior release: [v6.6.0](https://github.com/pistolwhip-justin/Iris-Pistolwhip-d/releases/tag/v6.6.0))
- **Tag:** `v6.6.1` points to the exact release source commit `cebe7a0d992c7e05d4b4e74f9269e492bfe8b587`; the v6.6.0 tag remains immutable.
- **Build/tests:** v6.6.1 NoMlKit debug APK built successfully; 8 JUnit tests passed with zero failures/errors.
- **Identity:** CI verified package `pro.pistolwhip.iris.keyboard`, label `Iris Keyboard Pistolwhip'd`, and provider authority `pro.pistolwhip.iris.keyboard.gifprovider`.
- **Artifact:** v6.6.1 APK was published with SHA-256 `287fff96fbd41b10ea2b0ea606f980a75c11183249ffbe0a0cde5765d2b52083`.
- **Not yet verified:** Physical installation beside upstream Iris, keyboard selection, live suggestions/corrections, custom vocabulary behavior, and the two-press backspace sequence on target hardware.


### Phase 4 — Expanded emoji library

**Status:** Planned only; implementation has not started. This phase was added after the user identified the current emoji choices as too limited. It is a future development phase, not part of the v6.6.1 release.

**Purpose**

Substantially expand the emoji choices available from the existing Iris keyboard while retaining the current keyboard layout and emoji-selection experience.

**Planned deliverables**

- Inventory the current emoji picker, its source data, and how emoji are inserted into the active editor.
- Select a maintained, versioned Unicode emoji data source; review its license and attribution requirements before incorporating it.
- Expand the available catalog substantially while preserving all existing choices and insertion behavior.
- Check Android/device font support and fallback behavior so unsupported glyphs do not break the picker or text entry.
- Document the Unicode data version, supported coverage, and any known device compatibility limits.

**Explicit boundaries**

The first pass is catalog expansion, not an assumed picker redesign. Search, categories beyond those already present, recently used items, favorites, skin-tone/gender variants, stickers, and custom emoji are not included as committed requirements. They may be evaluated separately if requested.

**Readiness / acceptance gate**

1. Confirm the current picker/data implementation and agree on the desired breadth of emoji coverage.
2. Approve the data source and its licensing/attribution.
3. Verify that existing emoji remain available and that the expanded catalog can be browsed and inserted into normal text fields.
4. Test rendering on the supported Android/device matrix, documenting glyphs that depend on newer system fonts.
5. Run automated tests/build checks and update the README with the delivered behavior before the phase is marked complete.

**Start condition:** Do not begin implementation until the current v6.6.1 device-testing feedback is received and the emoji data-source/scope choices are confirmed with the user.


### v6.6.2 — Device feedback fixes

**Status:** Source changes committed; release CI/device acceptance pending.

- Remove the blocking setup redirect from SettingsActivity so preferences remain accessible before the keyboard is enabled.
- Launch Settings from the keyboard with a simple new-task intent to avoid problematic task-stack flags on Samsung devices.
- Keep the candidate strip visible at a fixed 40dp height during ordinary typing; show next-word candidates from local bigram history or learned frequent words when available.
- Accept close spell-check candidates for conservative Space autocorrection even if the selected spell-checker omits optional typo/recommendation flags.
- Apply a theme background to the suggestion strip so it visually sits with the utility toolbar.

**Acceptance gate:** CI build and unit tests pass; user verifies settings entry from the keyboard toolbar and Android Settings, stable toolbar positioning, candidate tap, Space correction, and normal/backspace typing on device. Physical-device behavior remains unverified until tested.
