package railway.sim;

import java.util.List;

public interface Player {
    // Initialization function.
    // opponent: Name of the opponent.
    public void init(String name, double budget);

    // Gets skills of all 15 players.
    public Bid getBid(List<BidInfo> allBids);
}
