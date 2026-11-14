package app.service;

/** Outcome of submitting a report: its id and how many matches it found. */
public record SubmitResult(int reportId, int matchesFound) { }
