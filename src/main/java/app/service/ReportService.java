package app.service;

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

    public int submit(User user, ReportType type, String title,
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
        return reports.insert(user.id(), type, cleanTitle, category,
                cleanText, zone, date, photoPath);
    }

    public List<Report> listActive() {
        return reports.findActive();
    }

    /** The owner of a report, or an admin, may withdraw it. */
    public void withdraw(User actor, Report report) {
        boolean allowed = actor.role() == Role.ADMIN
                || actor.id() == report.userId();
        if (!allowed) {
            throw new ServiceException(
                    "You can only withdraw your own reports.");
        }
        reports.updateStatus(report.id(), ReportStatus.CLOSED);
    }
}
