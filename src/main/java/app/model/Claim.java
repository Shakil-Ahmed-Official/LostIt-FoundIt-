package app.model;

public record Claim(int id, int matchId, int claimantId,
                    String claimantName, String hiddenDetail,
                    ClaimStatus status, String pickupCode,
                    String createdAt) { }
