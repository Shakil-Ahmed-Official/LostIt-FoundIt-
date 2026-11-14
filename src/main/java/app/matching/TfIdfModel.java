package app.matching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TF-IDF weighting learned from a collection of documents.
 *
 * tf-idf(t, d) = tf(t, d) * idf(t),  idf(t) = ln((1 + N) / (1 + df(t))) + 1
 */
public final class TfIdfModel {

    private final int documentCount;
    private final Map<String, Integer> documentFrequency = new HashMap<>();

    public TfIdfModel(List<List<String>> documents) {
        this.documentCount = documents.size();
        for (List<String> document : documents) {
            for (String term : new HashSet<>(document)) {
                documentFrequency.merge(term, 1, Integer::sum);
            }
        }
    }

    public double idf(String term) {
        int df = documentFrequency.getOrDefault(term, 0);
        return Math.log((1.0 + documentCount) / (1.0 + df)) + 1.0;
    }

    /** Sparse TF-IDF vector of one document. */
    public Map<String, Double> vector(List<String> tokens) {
        Map<String, Double> vector = new HashMap<>();
        for (String term : tokens) {
            vector.merge(term, 1.0, Double::sum);
        }
        vector.replaceAll((term, tf) -> tf * idf(term));
        return vector;
    }

    /** Cosine similarity of two sparse vectors, from 0 (nothing in common)
     *  to 1 (same direction). */
    public static double cosine(Map<String, Double> a,
                                Map<String, Double> b) {
        double dot = 0;
        Set<String> smaller = a.size() <= b.size() ? a.keySet() : b.keySet();
        for (String term : smaller) {
            dot += a.getOrDefault(term, 0.0) * b.getOrDefault(term, 0.0);
        }
        double normA = norm(a);
        double normB = norm(b);
        return normA == 0 || normB == 0 ? 0 : dot / (normA * normB);
    }

    private static double norm(Map<String, Double> vector) {
        double sum = 0;
        for (double value : vector.values()) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}
