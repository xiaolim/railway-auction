package railway.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// To access data classes.
import railway.sim.utils.*;

public class Player implements railway.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;

    private double budget;

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(
        String name,
        double budget,
        List<Coordinates> geo,
        List<List<Integer>> infra,
        int[][] transit,
        List<String> townLookup) {
        
        this.name = name;
        this.budget = budget;
    }

    public Bid getBid(List<BidInfo> allBids) {
        List<BidInfo> availableBids = new ArrayList<>();

        for (BidInfo bi : allBids) {
            if (bi.owner == null) {
                availableBids.add(bi);
            }
        }

        BidInfo randomBid = availableBids.get(rand.nextInt(availableBids.size()));

        Bid bid = new Bid();
        bid.amount = randomBid.amount;
        bid.id1 = randomBid.id; 
    }
    
    public void updateBudget(int linkId1, int linkId2, double amount) {
        budget -= amount;
    }
}