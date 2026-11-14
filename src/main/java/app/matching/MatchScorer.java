package app.matching;

import app.model.Catalog;
import app.model.Report;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Filters and scores a lost report against a found report. */
public final class MatchScorer {

    public static final int MAX_DAYS = 14;
    public static final double W_TEXT = 0.70;
    public static final double W_ZONE = 0.15;
    public static final double W_DATE = 0.15;
    /** Pairs scoring below this are not shown to users. */
    public static final double THRESHOLD = 0.40;

    private MatchScorer() { }

    /** Layer 1: same category and dates at most MAX_DAYS apart. */
    public static boolean passesFilters(Report lost, Report found) {
        return lost.category().equals(found.category())
                && Math.abs(daysBetween(lost.eventDate(), found.eventDate()))
                        <= MAX_DAYS;
    }

    /** Text of a report; the title counts twice so it weighs more. */
    public static String textOf(Report report) {
        String description = report.description() == null
                ? "" : report.description();
        return report.title() + " " + report.title() + " " + description;
    }

    public static MatchScore score(Report lost, Report found,
                                   TfIdfModel model) {
        double text = TfIdfModel.cosine(
                model.vector(TextNormalizer.tokens(textOf(lost))),
                model.vector(TextNormalizer.tokens(textOf(found))));
        double zone = zoneScore(lost.zone(), found.zone());
        double date = dateScore(lost.eventDate(), found.eventDate());
        double total = W_TEXT * text + W_ZONE * zone + W_DATE * date;
        return new MatchScore(text, zone, date, total);
    }

    /** 1 for the same zone, 0.5 for nearby zones, otherwise 0. */
    public static double zoneScore(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        return Catalog.zoneCluster(a).equals(Catalog.zoneCluster(b))
                ? 0.5 : 0.0;
    }

    /** 1 for the same day, falling in a straight line to 0 at MAX_DAYS. */
    public static double dateScore(LocalDate a, LocalDate b) {
        long days = Math.abs(daysBetween(a, b));
        return Math.max(0.0, 1.0 - (double) days / MAX_DAYS);
    }

    private static long daysBetween(LocalDate a, LocalDate b) {
        return ChronoUnit.DAYS.between(a, b);
    }
}
