/*
 * Copyright (C) 2026 Pistol Whip Productions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nabu.iris.keyboard.latin;

/**
 * Tracks the narrow Backspace window immediately following automatic correction.
 * The original spelling is deliberately not restored: the second press removes one
 * character from the corrected word.
 */
public final class AutocorrectBackspaceState {
    public enum Action {
        DELETE_NORMALLY,
        DELETE_FINAL_CHARACTER
    }

    private String correctedWord;
    private boolean separatorRemoved;

    public void onCorrectionCommitted(final String word) {
        correctedWord = word;
        separatorRemoved = false;
    }

    public Action onBackspace(final String textBeforeCursor, final boolean hasSelection) {
        if (correctedWord == null || hasSelection) {
            clear();
            return Action.DELETE_NORMALLY;
        }

        final String before = textBeforeCursor == null ? "" : textBeforeCursor;
        if (!separatorRemoved) {
            if (before.endsWith(" ")) {
                separatorRemoved = true;
            } else {
                clear();
            }
            return Action.DELETE_NORMALLY;
        }

        if (before.endsWith(correctedWord)) {
            clear();
            return Action.DELETE_FINAL_CHARACTER;
        }

        clear();
        return Action.DELETE_NORMALLY;
    }

    public void clear() {
        correctedWord = null;
        separatorRemoved = false;
    }
}
