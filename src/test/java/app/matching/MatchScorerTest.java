package app.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.model.Report;
import app.model.ReportStatus;
import app.model.ReportType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchScorerTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 10);

    private static Report report(ReportType type, String title, String text,
                                 String category, String zone,
                                 LocalDate date) {
        return new Report(1, 1, "Test", type, title, category, text, zone,
                date, null, ReportStatus.OPEN, "");
    }

    private static TfIdfModel modelOf(Report... reports) {
        List<List<String>> corpus = new ArrayList<>();
        for (Report r : reports) {
            corpus.add(TextNormalizer.tokens(MatchScorer.textOf(r)));
        }
        return new TfIdfModel(corpus);
    }

    @Test
    void weightsSumToOne() {
        assertEquals(1.0, MatchScorer.W_TEXT + MatchScorer.W_ZONE
                + MatchScorer.W_DATE, 1e-9);
    }

    @Test
    void zoneScoreIsOneForTheSameZone() {
        assertEquals(1.0, MatchScorer.zoneScore("Library", "Library"), 1e-9);
    }

    @Test
    void zoneScoreIsHalfForNearbyZones() {
        assertEquals(0.5,
                MatchScorer.zoneScore("Library", "Main Building"), 1e-9);
    }

    @Test
    void zoneScoreIsZeroForFarZones() {
        assertEquals(0.0, MatchScorer.zoneScore("Library", "Hostel"), 1e-9);
    }

    @Test
    void dateScoreFallsInAStraightLine() {
        assertEquals(1.0, MatchScorer.dateScore(DAY, DAY), 1e-9);
        assertEquals(0.5, MatchScorer.dateScore(DAY, DAY.plusDays(7)), 1e-9);
        assertEquals(0.0, MatchScorer.dateScore(DAY, DAY.plusDays(14)), 1e-9);
        assertEquals(0.0, MatchScorer.dateScore(DAY, DAY.plusDays(20)), 1e-9);
    }

    @Test
    void filterRejectsDifferentCategories() {
        Report lost = report(ReportType.LOST, "Phone", "", "Phone",
                "Library", DAY);
        Report found = report(ReportType.FOUND, "Wallet", "", "Wallet",
                "Library", DAY);
        assertFalse(MatchScorer.passesFilters(lost, found));
    }

    @Test
    void filterUsesAFourteenDayWindow() {
        Report lost = report(ReportType.LOST, "Phone", "", "Phone",
                "Library", DAY);
        Report inside = report(ReportType.FOUND, "Phone", "", "Phone",
                "Library", DAY.plusDays(14));
        Report outside = report(ReportType.FOUND, "Phone", "", "Phone",
                "Library", DAY.plusDays(15));
        assertTrue(MatchScorer.passesFilters(lost, inside));
        assertFalse(MatchScorer.passesFilters(lost, outside));
    }

    @Test
    void similarDescriptionsScoreAboveTheThreshold() {
        Report lost = report(ReportType.LOST, "Blue Nike water bottle",
                "Blue sports bottle with a dent", "Bottle", "Library", DAY);
        Report found = report(ReportType.FOUND, "Blue sports flask",
                "Blue plastic flask with a dent", "Bottle", "Library",
                DAY.plusDays(1));
        MatchScore s = MatchScorer.score(lost, found, modelOf(lost, found));
        assertTrue(s.total() >= MatchScorer.THRESHOLD);
    }

    @Test
    void unrelatedDescriptionsScoreBelowTheThreshold() {
        Report lost = report(ReportType.LOST, "Red folding umbrella",
                "Red umbrella with a wooden handle", "Other", "Library", DAY);
        Report found = report(ReportType.FOUND, "Green math notebook",
                "Green notebook with algebra notes", "Other", "Hostel",
                DAY.plusDays(10));
        MatchScore s = MatchScorer.score(lost, found, modelOf(lost, found));
        assertTrue(s.total() < MatchScorer.THRESHOLD);
    }

    @Test
    void totalIsTheWeightedSumOfThePartScores() {
        Report lost = report(ReportType.LOST, "Black phone", "Black phone",
                "Phone", "Library", DAY);
        Report found = report(ReportType.FOUND, "Black phone", "Black phone",
                "Phone", "Main Building", DAY.plusDays(7));
        MatchScore s = MatchScorer.score(lost, found, modelOf(lost, found));
        double expected = MatchScorer.W_TEXT * s.text()
                + MatchScorer.W_ZONE * s.zone()
                + MatchScorer.W_DATE * s.date();
        assertEquals(expected, s.total(), 1e-9);
    }
}
