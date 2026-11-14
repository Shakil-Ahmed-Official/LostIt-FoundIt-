package app.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextNormalizerTest {

    @Test
    void lowercasesAndRemovesPunctuation() {
        assertEquals(List.of("blue", "nike", "bottle"),
                TextNormalizer.tokens("Blue, NIKE bottle!"));
    }

    @Test
    void removesStopWords() {
        assertEquals(List.of("wallet", "library"),
                TextNormalizer.tokens("I lost my wallet in the library"));
    }

    @Test
    void removesPluralEndings() {
        assertEquals(List.of("key", "bottle"),
                TextNormalizer.tokens("keys bottles"));
    }

    @Test
    void keepsWordsEndingInDoubleS() {
        assertEquals("class", TextNormalizer.stem("class"));
    }

    @Test
    void mapsWordsWithTheSameMeaningToOneWord() {
        assertEquals(TextNormalizer.tokens("bottle"),
                TextNormalizer.tokens("flask"));
        assertEquals(TextNormalizer.tokens("phone"),
                TextNormalizer.tokens("Mobile"));
        assertEquals(TextNormalizer.tokens("wallet"),
                TextNormalizer.tokens("purse"));
    }

    @Test
    void handlesNullAndEmptyText() {
        assertTrue(TextNormalizer.tokens(null).isEmpty());
        assertTrue(TextNormalizer.tokens("   ").isEmpty());
    }
}
