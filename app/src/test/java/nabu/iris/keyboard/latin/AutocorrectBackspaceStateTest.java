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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AutocorrectBackspaceStateTest {
    @Test
    public void firstBackspaceDeletesOnlyInsertedSpaceAndSecondDeletesCorrectedFinalLetter() {
        AutocorrectBackspaceState state = new AutocorrectBackspaceState();
        state.onCorrectionCommitted("the");

        assertEquals(AutocorrectBackspaceState.Action.DELETE_NORMALLY,
                state.onBackspace("the ", false));
        assertEquals(AutocorrectBackspaceState.Action.DELETE_FINAL_CHARACTER,
                state.onBackspace("the", false));
    }

    @Test
    public void secondBackspaceDoesNotRestoreOriginalMisspelling() {
        AutocorrectBackspaceState state = new AutocorrectBackspaceState();
        state.onCorrectionCommitted("the");
        state.onBackspace("the ", false);

        assertEquals(AutocorrectBackspaceState.Action.DELETE_FINAL_CHARACTER,
                state.onBackspace("the", false));
    }

    @Test
    public void selectionInvalidatesPendingCorrection() {
        AutocorrectBackspaceState state = new AutocorrectBackspaceState();
        state.onCorrectionCommitted("the");

        assertEquals(AutocorrectBackspaceState.Action.DELETE_NORMALLY,
                state.onBackspace("the ", true));
        assertEquals(AutocorrectBackspaceState.Action.DELETE_NORMALLY,
                state.onBackspace("the", false));
    }

    @Test
    public void interveningEditInvalidatesPendingCorrection() {
        AutocorrectBackspaceState state = new AutocorrectBackspaceState();
        state.onCorrectionCommitted("the");
        state.onBackspace("the ", false);

        assertEquals(AutocorrectBackspaceState.Action.DELETE_NORMALLY,
                state.onBackspace("thex", false));
    }
}
