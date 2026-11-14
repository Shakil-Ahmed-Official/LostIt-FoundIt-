package app.model;

public record Match(int id, int lostReportId, int foundReportId,
                    double textScore, double finalScore,
                    MatchStatus status, String createdAt) { }
