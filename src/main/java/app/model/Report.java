package app.model;

import java.time.LocalDate;

public record Report(int id, int userId, String reporterName,
                     ReportType type, String title, String category,
                     String description, String zone, LocalDate eventDate,
                     String photoPath, ReportStatus status,
                     String createdAt) { }
