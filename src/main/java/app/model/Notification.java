package app.model;

public record Notification(int id, int userId, String message,
                           boolean read, String createdAt) { }
