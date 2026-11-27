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

public class AutocorrectBarControllerTest {
    @Test
    public void lastWordReturnsTrailingWord() {
        assertEquals("keyboard", AutocorrectBarController.lastWord("I like this keyboard"));
    }

    @Test
    public void lastWordSupportsApostrophes() {
        assertEquals("don't", AutocorrectBarController.lastWord("I don't"));
    }

    @Test
    public void editDistanceRecognizesCommonTranspositionAsTwoEdits() {
        assertEquals(2, AutocorrectBarController.editDistance("teh", "the"));
    }

    @Test
    public void editDistanceReturnsZeroForIdenticalWords() {
        assertEquals(0, AutocorrectBarController.editDistance("keyboard", "keyboard"));
    }

    @Test
    public void autocorrectAllowsTwoEditCorrectionsForShortWords() {
        assertEquals(2, AutocorrectBarController.maximumCorrectionDistance("wht"));
        assertEquals(2, AutocorrectBarController.maximumCorrectionDistance("correct"));
    }

    @Test
    public void autocorrectAllowsThreeEditCorrectionsForLongWords() {
        assertEquals(3, AutocorrectBarController.maximumCorrectionDistance("populstex"));
    }
}
