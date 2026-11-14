package app.service;

import app.dao.MatchDao;
import app.dao.NotificationDao;
import app.dao.ReportDao;
import app.matching.MatchScore;
import app.matching.MatchScorer;
import app.matching.TextNormalizer;
import app.matching.TfIdfModel;
import app.model.Report;
import app.model.ReportType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Compares a new report with open reports of the opposite type. */
public class MatchingService {

    /** A candidate pair with its scores. */
    public record Candidate(Report lost, Report found, MatchScore score) { }

    private final ReportDao reports = new ReportDao();
    private final MatchDao matches = new MatchDao();
    private final NotificationDao notifications = new NotificationDao();

    /** Layers 1 to 3: filter, text similarity and combined score. */
    public List<Candidate> rank(Report report) {
        boolean isLost = report.type() == ReportType.LOST;
        ReportType wanted = isLost ? ReportType.FOUND : ReportType.LOST;
        LocalDate from = report.eventDate().minusDays(MatchScorer.MAX_DAYS);
        LocalDate to = report.eventDate().plusDays(MatchScorer.MAX_DAYS);

        List<Report> others = reports.findCandidates(wanted,
                report.category(), from, to, report.userId());
        if (others.isEmpty()) {
            return List.of();
        }

        // word weights are learned from every report that is still open
        List<List<String>> corpus = new ArrayList<>();
        for (Report r : reports.findActive()) {
            corpus.add(TextNormalizer.tokens(MatchScorer.textOf(r)));
        }
        TfIdfModel model = new TfIdfModel(corpus);

        List<Candidate> ranked = new ArrayList<>();
        for (Report other : others) {
            Report lost = isLost ? report : other;
            Report found = isLost ? other : report;
            ranked.add(new Candidate(lost, found,
                    MatchScorer.score(lost, found, model)));
        }
        ranked.sort(Comparator.comparingDouble(
                (Candidate c) -> c.score().total()).reversed());
        return ranked;
    }

    /** Saves the matches above the threshold and tells both users. */
    public int matchNewReport(Report report) {
        int created = 0;
        for (Candidate c : rank(report)) {
            if (c.score().total() < MatchScorer.THRESHOLD) {
                continue;
            }
            int id = matches.insertIfAbsent(c.lost().id(), c.found().id(),
                    c.score().text(), c.score().total());
            if (id < 0) {
                continue;
            }
            created++;
            int percent = (int) Math.round(c.score().total() * 100);
            notifications.insert(c.lost().userId(),
                    "A found report may match your lost item \""
                    + c.lost().title() + "\" (" + percent
                    + "% match). Open the Match inbox to compare them.");
            notifications.insert(c.found().userId(),
                    "Your found item \"" + c.found().title()
                    + "\" may match a lost report (" + percent
                    + "% match). The owner has been notified.");
        }
        return created;
    }
}
