package app.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TfIdfModelTest {

    private final TfIdfModel model = new TfIdfModel(List.of(
            List.of("blue", "bottle"),
            List.of("blue", "phone"),
            List.of("blue", "wallet")));

    @Test
    void rarerTermsGetHigherWeight() {
        assertTrue(model.idf("bottle") > model.idf("blue"));
    }

    @Test
    void identicalTextsHaveCosineOne() {
        Map<String, Double> v = model.vector(List.of("blue", "bottle"));
        assertEquals(1.0, TfIdfModel.cosine(v, v), 1e-9);
    }

    @Test
    void textsWithoutCommonWordsHaveCosineZero() {
        Map<String, Double> a = model.vector(List.of("bottle"));
        Map<String, Double> b = model.vector(List.of("wallet"));
        assertEquals(0.0, TfIdfModel.cosine(a, b), 1e-9);
    }

    @Test
    void cosineDoesNotDependOnTheOrderOfTheTexts() {
        Map<String, Double> a = model.vector(List.of("blue", "bottle"));
        Map<String, Double> b = model.vector(List.of("blue", "phone"));
        assertEquals(TfIdfModel.cosine(a, b), TfIdfModel.cosine(b, a), 1e-9);
    }

    @Test
    void sharedRareWordScoresHigherThanSharedCommonWord() {
        Map<String, Double> base = model.vector(List.of("blue", "bottle"));
        Map<String, Double> rare = model.vector(List.of("bottle", "red"));
        Map<String, Double> common = model.vector(List.of("blue", "red"));
        assertTrue(TfIdfModel.cosine(base, rare)
                > TfIdfModel.cosine(base, common));
    }

    @Test
    void emptyTextGivesZero() {
        Map<String, Double> v = model.vector(List.of("blue"));
        assertEquals(0.0, TfIdfModel.cosine(v, model.vector(List.of())), 1e-9);
    }
}
