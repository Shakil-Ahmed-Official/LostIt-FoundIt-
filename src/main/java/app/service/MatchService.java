package app.service;

import app.dao.ClaimDao;
import app.dao.MatchDao;
import app.model.MatchStatus;
import app.model.MatchView;
import app.model.User;

import java.util.ArrayList;
import java.util.List;

/** The match inbox: reading matches and dismissing them. */
public class MatchService {

    private final MatchDao matches = new MatchDao();
    private final ClaimDao claims = new ClaimDao();

    /** Matches of this user; an admin sees all matches. */
    public List<MatchView> listFor(User user) {
        List<MatchView> found = Session.isAdmin()
                ? matches.findAll() : matches.findForUser(user.id());
        List<MatchView> result = new ArrayList<>();
        for (MatchView view : found) {
            result.add(withClaim(view));
        }
        return result;
    }

    public MatchView reload(int matchId) {
        return matches.findViewById(matchId)
                .map(this::withClaim)
                .orElseThrow(() -> new ServiceException(
                        "This match no longer exists."));
    }

    /** The owner of the lost report says that this is not their item. */
    public void dismiss(User user, int matchId) {
        MatchView view = reload(matchId);
        if (view.lost().userId() != user.id()) {
            throw new ServiceException(
                    "Only the owner of the lost report can dismiss a match.");
        }
        if (view.match().status() != MatchStatus.PENDING
                || view.claim() != null) {
            throw new ServiceException(
                    "This match can no longer be dismissed.");
        }
        matches.updateStatus(matchId, MatchStatus.REJECTED);
    }

    private MatchView withClaim(MatchView view) {
        return new MatchView(view.match(), view.lost(), view.found(),
                claims.findLatestByMatch(view.match().id()).orElse(null));
    }
}
