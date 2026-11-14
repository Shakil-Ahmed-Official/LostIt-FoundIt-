package app.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Turns free text into comparable tokens: lower case, no stop words, plural
 *  endings removed, and words with the same meaning mapped to one word. */
public final class TextNormalizer {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "of", "in", "on", "at", "to",
            "my", "is", "was", "with", "it", "this", "that", "near", "by",
            "for", "from", "i", "me", "has", "have", "had", "found", "lost",
            "some", "inside", "left", "very", "one", "its", "are", "were",
            "there", "your", "our", "can", "if", "but", "not", "no");

    private static final Map<String, String> SYNONYMS = buildSynonyms();

    private TextNormalizer() { }

    public static List<String> tokens(String text) {
        List<String> result = new ArrayList<>();
        if (text == null) {
            return result;
        }
        String clean = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ");
        for (String word : clean.trim().split(" ")) {
            if (word.length() < 2 || STOP_WORDS.contains(word)) {
                continue;
            }
            result.add(canonical(word));
        }
        return result;
    }

    static String canonical(String word) {
        String direct = SYNONYMS.get(word);
        if (direct != null) {
            return direct;
        }
        String stem = stem(word);
        return SYNONYMS.getOrDefault(stem, stem);
    }

    static String stem(String word) {
        if (word.length() > 3 && word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private static Map<String, String> buildSynonyms() {
        String[][] groups = {
                {"bottle", "flask", "thermos", "tumbler", "canteen", "jug"},
                {"phone", "mobile", "cellphone", "smartphone", "iphone",
                        "handset"},
                {"wallet", "purse", "billfold"},
                {"bag", "backpack", "satchel", "rucksack", "handbag", "tote"},
                {"key", "keychain", "keyring"},
                {"card", "idcard"},
                {"earphone", "earbud", "headphone", "airpod", "headset"},
                {"charger", "adapter", "adaptor"},
                {"laptop", "macbook", "chromebook"},
                {"umbrella", "brolly"},
                {"glass", "glasses", "spectacle", "eyeglass", "sunglass",
                        "sunglasses"},
                {"jacket", "coat", "hoodie", "sweater"},
                {"notebook", "copy", "diary", "register"},
        };
        Map<String, String> map = new HashMap<>();
        for (String[] group : groups) {
            for (String word : group) {
                map.put(word, group[0]);
            }
        }
        return Map.copyOf(map);
    }
}
