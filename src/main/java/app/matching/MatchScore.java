package app.matching;

/** The three part scores (each 0 to 1) and the combined score. */
public record MatchScore(double text, double zone, double date,
                         double total) { }
