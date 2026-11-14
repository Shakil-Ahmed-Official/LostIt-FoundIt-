package app.model;

/** A match together with both reports and its latest claim (may be null). */
public record MatchView(Match match, Report lost, Report found,
                        Claim claim) { }
