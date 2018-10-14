package railway.sim;

import java.util.List;
import railway.sim.utils.*;

public interface Player {
    // Initialization function.
    // name: name of current player.
    // budget: maximum amount allocated for bidding.
    // geo: Town indices and their coordinates.
    // infra: Town indices and other towns they are connected to.
    // transit: Town indices and the number of passengers between them.
    // townLookup: Town indices and names of towns.
    public void init(String name,
        double budget,
        List<Coordinates> geo,
        List<List<Integer>> infra,
        int[][] transit,
        List<String> townLookup);

    // The bid placed for a round.
    // Returns null if they don't want to place a bid.
    public Bid getBid(List<BidInfo> allBids);

    // Indicates to the player that they have won that link/pair of links.
    public void updateBudget(int linkId1, int linkId2, double amount);
}
