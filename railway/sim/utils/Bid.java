package railway.sim.utils;

public class Bid {
    // The ids of links that are being bid.
    public int id1;

    // To be set only if the player is bidding for a pair of links.
    public int id2;

    // The amount that the link was bid for.
    public double amount;

    public Bid() {
        id1 = -1;
        id2 = -1;
    }
}