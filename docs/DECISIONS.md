# 🧾 Decision Record

## ADR-001 — Side-by-side application identity

**Date:** 2026-11-22  
**Decision:** Use `pro.pistolwhip.iris.keyboard` as the derivative application ID and `Iris Keyboard Pistolwhip'd` as the system-visible app name.  
**Reason:** Android cannot install two applications with the same application ID as separate apps. The upstream package is `nabu.iris.keyboard`; the fork must use a distinct ID to satisfy the explicit side-by-side installation requirement.  
**Status:** Accepted.

## ADR-002 — Candidate strip placement

**Date:** 2026-11-22  
**Decision:** Place three candidate slots directly below Iris's existing utility toolbar and above the keyboard keys.  
**Reason:** Preserve the current toolbar and keyboard while matching the requested SwiftKey-like layout.  
**Status:** Accepted.

## ADR-003 — Backspace behavior

**Date:** 2026-11-22  
**Decision:** Automatic correction commits the corrected word before the normal Space key is inserted. Do not add a separate undo button or intercept backspace for a special undo action.  
**Expected behavior:** Backspace once deletes only the space; a second press deletes the final character of the corrected word.  
**Status:** Accepted.

## ADR-004 — Spell-check source and privacy boundary

**Date:** 2026-11-22  
**Decision:** Use Android's selected spell-checker service for base spelling candidates, while keeping personal vocabulary and prediction frequencies in the fork's own device-protected preferences. Do not call an AI endpoint for ordinary autocorrect.  
**Reason:** Reuse the Android language service instead of shipping an unverified dictionary or remote model. The selected spell-checker is a separate system component; its own network/privacy behavior is device-dependent and must be disclosed.  
**Status:** Accepted, with device validation required.

## ADR-005 — Personal vocabulary learning

**Date:** 2026-11-22  
**Decision:** Store custom words and locally learned words in the fork's app preferences. Learn repeatedly used words and word-pair frequencies; provide a manual comma-/line-separated custom-word setting.  
**Reason:** Preserve the user's custom spelling without requiring a cloud account or AI endpoint.  
**Status:** Accepted.

## ADR-006 — Fork-owned release and update path

**Date:** 2026-11-22  
**Decision:** The About screen's GitHub link and release documentation point to this fork. The inherited generic release workflow and upstream F-Droid metadata were removed. No in-app updater was identified in the inspected source tree.  
**Reason:** The derivative must never direct users to install upstream APKs over or instead of this fork. Versioned GitHub Releases are the canonical update source.  
**Status:** Accepted.

## ADR-007 — Package-derived GIF provider authority

**Date:** 2026-11-22  
**Decision:** The manifest provider authority uses the `${applicationId}` placeholder, and GIF content URIs use `Context.getPackageName()`.  
**Reason:** A separate application ID alone is not enough if a content-provider authority remains hard-coded to upstream; both apps must be able to coexist without provider collisions.  
**Status:** Accepted.


## ADR-008 — Stable suggestion strip and settings task launch

**Date:** 2026-11-27  
**Decision:** Keep the autocorrect row at a stable height during ordinary typing, populate next-word candidates from local history when available, and accept close Android spell-checker candidates for conservative Space correction even when the spell-checker omits optional recommendation flags. Settings launches use a simple new-task intent; the settings activity no longer blocks access by redirecting users to system IME settings before showing preferences.  
**Reason:** The v6.6.1 device feedback identified toolbar movement, unreliable Space correction, and settings navigation failures on Samsung Android.  
**Status:** Implemented in v6.6.2 source; CI and device validation pending.


## ADR-009 — Visible candidates and asynchronous Space autocorrect

**Date:** 2026-11-27  
**Decision:** Use explicit theme foreground colors for candidate text. When Space arrives before the Android spell-checker response, request a fresh correction and apply it after the separator only if the editor still ends with that exact word and space. Allow a slightly wider edit distance for longer spell-checker-proposed words.  
**Reason:** v6.6.2 feedback confirmed that candidate text blended into the strip background and that cached asynchronous results were not reliably available at Space time.  
**Status:** Implemented in v6.6.3 source; CI and physical-device validation pending.
