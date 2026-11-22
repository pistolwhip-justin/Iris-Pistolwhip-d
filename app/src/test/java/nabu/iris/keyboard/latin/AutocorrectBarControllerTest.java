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
}
