package app.service;

import app.dao.ClaimDao;
import app.dao.HandoverDao;
import app.dao.MatchDao;
import app.dao.NotificationDao;
import app.dao.ReportDao;
import app.dao.UserDao;
import app.model.Claim;
import app.model.ClaimStatus;
import app.model.ClaimView;
import app.model.MatchStatus;
import app.model.MatchView;
import app.model.ReportStatus;
import app.model.Role;
import app.model.User;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/** Claim, verification and pickup workflow. */
public class ClaimService {

    /** No 0, O, 1, I or L, so a code is easy to read aloud. */
    private static final String CODE_CHARS =
            "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MatchService matchService = new MatchService();
    private final MatchDao matches = new MatchDao();
    private final ClaimDao claims = new ClaimDao();
    private final ReportDao reports = new ReportDao();
    private final HandoverDao handovers = new HandoverDao();
    private final NotificationDao notifications = new NotificationDao();
    private final UserDao users = new UserDao();

    /** The owner of the lost report claims the found item. */
    public Claim submit(User user, int matchId, String hiddenDetail) {
        MatchView view = matchService.reload(matchId);
        if (view.lost().userId() != user.id()) {
            throw new ServiceException(
                    "Only the owner of the lost report can claim this item.");
        }
        if (view.match().status() != MatchStatus.PENDING
                || view.claim() != null) {
            throw new ServiceException("This match is no longer open.");
        }
        if (view.lost().status() != ReportStatus.OPEN
                || view.found().status() != ReportStatus.OPEN) {
            throw new ServiceException(
                    "One of the reports is already part of another claim.");
        }
        String detail = hiddenDetail == null ? "" : hiddenDetail.trim();
        if (detail.length() < 10) {
            throw new ServiceException("Please describe a hidden detail "
                    + "in at least 10 characters.");
        }
        if (detail.length() > 500) {
            throw new ServiceException(
                    "The description must be 500 characters or fewer.");
        }

        int id = claims.insert(matchId, user.id(), detail);
        reports.updateStatus(view.lost().id(), ReportStatus.MATCHED);
        reports.updateStatus(view.found().id(), ReportStatus.MATCHED);
        for (int adminId : users.findIdsByRole(Role.ADMIN)) {
            notifications.insert(adminId, user.fullName() + " claimed \""
                    + view.found().title() + "\". Review the claim.");
        }
        return claims.findById(id).orElseThrow();
    }

    /** Admin accepts the hidden detail; a pickup code is generated. */
    public Claim approve(User admin, int claimId) {
        requireAdmin(admin);
        Claim claim = openClaim(claimId);
        MatchView view = matchService.reload(claim.matchId());

        String code = newPickupCode();
        claims.setApproved(claimId, code);
        matches.updateStatus(claim.matchId(), MatchStatus.CONFIRMED);
        reports.updateStatus(view.lost().id(), ReportStatus.CLAIMED);
        reports.updateStatus(view.found().id(), ReportStatus.CLAIMED);

        notifications.insert(claim.claimantId(), "Your claim for \""
                + view.found().title() + "\" was approved. Pickup code: "
                + code + ". Show it at the admin office.");
        notifications.insert(view.found().userId(), "The owner of \""
                + view.found().title() + "\" has been verified. Please "
                + "hand the item to the admin office.");
        return claims.findById(claimId).orElseThrow();
    }

    /** Admin rejects the claim; both reports become open again. */
    public void reject(User admin, int claimId) {
        requireAdmin(admin);
        Claim claim = openClaim(claimId);
        MatchView view = matchService.reload(claim.matchId());

        claims.setRejected(claimId);
        matches.updateStatus(claim.matchId(), MatchStatus.REJECTED);
        reports.updateStatus(view.lost().id(), ReportStatus.OPEN);
        reports.updateStatus(view.found().id(), ReportStatus.OPEN);
        notifications.insert(claim.claimantId(), "Your claim for \""
                + view.found().title() + "\" was not approved.");
    }

    /** Admin hands the item over after checking the pickup code. */
    public void recordHandover(User admin, int claimId, String code) {
        requireAdmin(admin);
        Claim claim = claims.findById(claimId).orElseThrow(
                () -> new ServiceException("This claim no longer exists."));
        boolean waiting = claims.findAwaitingHandover().stream()
                .anyMatch(c -> c.id() == claimId);
        if (!waiting) {
            throw new ServiceException(
                    "This claim is not ready for handover.");
        }
        String entered = code == null ? "" : code.trim();
        if (!entered.equalsIgnoreCase(claim.pickupCode())) {
            throw new ServiceException("The pickup code does not match.");
        }

        MatchView view = matchService.reload(claim.matchId());
        handovers.insert(claimId, admin.id());
        reports.updateStatus(view.lost().id(), ReportStatus.RETURNED);
        reports.updateStatus(view.found().id(), ReportStatus.RETURNED);
        notifications.insert(claim.claimantId(), "\"" + view.lost().title()
                + "\" was handed back to you. Welcome back, item!");
        notifications.insert(view.found().userId(), "Thank you! \""
                + view.found().title()
                + "\" has been returned to its owner.");
    }

    public List<ClaimView> pending(User admin) {
        requireAdmin(admin);
        return toViews(claims.findByStatus(ClaimStatus.SUBMITTED));
    }

    public List<ClaimView> awaitingHandover(User admin) {
        requireAdmin(admin);
        return toViews(claims.findAwaitingHandover());
    }

    private List<ClaimView> toViews(List<Claim> list) {
        List<ClaimView> views = new ArrayList<>();
        for (Claim claim : list) {
            MatchView match = matchService.reload(claim.matchId());
            views.add(new ClaimView(claim, match));
        }
        return views;
    }

    private Claim openClaim(int claimId) {
        Claim claim = claims.findById(claimId).orElseThrow(
                () -> new ServiceException("This claim no longer exists."));
        if (claim.status() != ClaimStatus.SUBMITTED) {
            throw new ServiceException("This claim was already reviewed.");
        }
        return claim;
    }

    private static void requireAdmin(User user) {
        if (user == null || user.role() != Role.ADMIN) {
            throw new ServiceException("Only an admin can do this.");
        }
    }

    private static String newPickupCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(CODE_CHARS.charAt(
                    RANDOM.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }
}
