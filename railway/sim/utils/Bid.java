package railway.sim.utils;

import java.io.Serializable;

public class Bid implements Serializable {
    // The ids of links that are being bid.
    public int id1;

    // To be set only if the player is bidding for a pair of links.
    public int id2;

    // The amount that the link was bid for.
    public double amount;

    // The player that bid for this.
    public String bidder;

    public Bid() {
        id1 = -1;
        id2 = -1;
    }
}
