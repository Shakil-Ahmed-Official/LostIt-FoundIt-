package app.model;

public enum ReportType {
    LOST("Lost"), FOUND("Found");

    private final String label;

    ReportType(String label) { this.label = label; }

    public String label() { return label; }
}
