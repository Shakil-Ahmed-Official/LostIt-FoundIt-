package app.service;

import app.dao.MatchDao;
import app.dao.ReportDao;
import app.model.Report;
import app.model.ReportStatus;
import app.model.ReportType;
import app.model.Role;
import app.model.User;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportDao reports = new ReportDao();
    private final MatchDao matches = new MatchDao();
    private final MatchingService matching = new MatchingService();

    public SubmitResult submit(User user, ReportType type, String title,
                      String category, String description, String zone,
                      LocalDate date, File photo) {
        String cleanTitle = title == null ? "" : title.trim();
        String cleanText = description == null ? "" : description.trim();

        if (cleanTitle.length() < 3) {
            throw new ServiceException(
                    "Please enter a title of at least 3 characters.");
        }
        if (cleanTitle.length() > 80) {
            throw new ServiceException(
                    "The title must be 80 characters or fewer.");
        }
        if (category == null) {
            throw new ServiceException("Please choose a category.");
        }
        if (zone == null) {
            throw new ServiceException("Please choose a campus zone.");
        }
        if (date == null) {
            throw new ServiceException("Please choose a date.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ServiceException("The date cannot be in the future.");
        }
        if (cleanText.length() > 500) {
            throw new ServiceException(
                    "The description must be 500 characters or fewer.");
        }

        String photoPath = photo == null ? null : PhotoStore.save(photo);
        int id = reports.insert(user.id(), type, cleanTitle, category,
                cleanText, zone, date, photoPath);

        int found = 0;
        try {
            found = matching.matchNewReport(
                    reports.findById(id).orElseThrow());
        } catch (RuntimeException e) {
            // a matching problem must never lose the report itself
            e.printStackTrace();
        }
        return new SubmitResult(id, found);
    }

    public List<Report> listActive() {
        return reports.findActive();
    }

    /** The owner of a report, or an admin, may withdraw an OPEN report. */
    public void withdraw(User actor, Report report) {
        // read the report again: the copy on screen may be out of date
        Report current = reports.findById(report.id()).orElseThrow(
                () -> new ServiceException("This report no longer exists."));
        boolean allowed = actor.role() == Role.ADMIN
                || actor.id() == current.userId();
        if (!allowed) {
            throw new ServiceException(
                    "You can only withdraw your own reports.");
        }
        if (current.status() != ReportStatus.OPEN) {
            throw new ServiceException("This report is already part of a "
                    + "claim and cannot be withdrawn.");
        }
        reports.updateStatus(current.id(), ReportStatus.CLOSED);
        matches.rejectPendingFor(current.id());
    }
}
