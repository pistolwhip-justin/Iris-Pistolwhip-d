# AI Agent Instructions — Iris Keyboard Pistolwhip'd

This repository is an independently maintained derivative of Iris Keyboard. Read this file and the root README before making changes.

## Global development standard

The user's global development protocols are maintained in the private repository:

`pistolwhip-justin/development-protocols-n-assets`

Before any development session, read its current `DEVELOPMENT_PROTOCOLS.md` and `AGENTS.md` when access is available. Do not rely on a copied or remembered protocol version. Do not copy private historical archives, secrets, or internal material into this public repository.

## Project identity and upstream obligations

- Canonical derivative name: **Iris Keyboard Pistolwhip'd**.
- Upstream project: [Iris Keyboard](https://github.com/MohamadOday/Iris).
- Preserve upstream Apache-2.0 notices and attribution.
- The upstream application ID is `nabu.iris.keyboard`.
- This derivative uses `pro.pistolwhip.iris.keyboard` because the user explicitly requires installing it beside the original app. Do not change this application ID, the `nabu.iris.keyboard` source namespace, or provider authority without a documented technical reason and approval.
- There is no inherited in-app updater identified in the source tree. The About screen's GitHub link and all release links must point to this derivative repository. Never add an updater that offers upstream APKs.

## Product requirements

- Preserve existing Iris functionality unless a specific change requires otherwise.
- The autocorrect strip belongs between the existing utility toolbar and the keyboard keys.
- Use Android's selected spell-checker service for base spelling suggestions. Personal vocabulary and learned word-pair frequencies remain in device-protected app preferences. Never route ordinary autocorrect through OpenRouter or another AI/network API.
- Password, email, URL, and fields explicitly suppressing suggestions must not show candidates.
- Automatic correction is conservative and occurs before Space is committed.
- Backspace behavior is deliberately ordinary: one press removes the inserted space; a second press deletes the final character of the corrected word. Do not add a separate undo button.
- Custom words are entered in Preferences → Autocorrect & Learning. Keep settings and runtime preferences in the same device-protected preference store.
- Document the fact that Android's selected spell-checker is a separate system component and may have its own privacy/network behavior.

## Build and release

- Use the repository's Gradle wrapper and JDK 17.
- Preferred build: `./gradlew --no-daemon testNomlkitDebugUnitTest assembleNomlkitDebug`.
- Do not commit generated APKs.
- Each version must have its own workflow under `.github/workflows/release-<version>.yml`.
- Publish APKs as immutable GitHub Release assets, verify tag-to-source commit identity, and preserve previous releases.
- Distinguish workflow completion, build success, automated test results, release publication, physical installation, and device behavior. Never claim device verification without an actual device test.
- Debug-signed builds are for sideload testing only. A stable signing key is required before future APKs can update the same installed fork in place.

## Documentation and handoff

- Update the root README alongside every phase.
- Keep `docs/DEVELOPMENT_PLAN.md` and `docs/DECISIONS.md consistent with implementation.
- Review tests, release workflow, and known limitations before calling a phase complete.
- Do not claim a build, test, install, or release succeeded unless verified from the actual tool/Actions result.
