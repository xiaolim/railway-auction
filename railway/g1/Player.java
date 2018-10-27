package railway.g1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// To access data classes.
import railway.sim.utils.*;

public class Player implements railway.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand = new Random(seed);

    private double budget;

    private List<BidInfo> availableBids = new ArrayList<>();
    
    // The coordinates of stations, infrastructure and raw transit files are stored for future reference.
    List<Coordinates> geo;
    List<List<Integer>> infra;
    int[][] transit;
    
    // Keep track of player owned links. It maps a link infra.get(i).get(j), denoted by an integer 
    // pair [i, j], to the player who owns that link (null if it is not sold yet). 
    Map<Pair, String> playerOwnedLinks;
    
    
    
    
    public class Pair {
    	int i1;
    	int i2;
    	Pair(int i1, int i2){
    		this.i1 = i1;
    		this.i2 = i2;
    	}
    }
    

    public Player() {
        rand = new Random();
    }

    public void init(String name, double budget, List<Coordinates> geo, 
    				List<List<Integer>> infra, int[][] transit, List<String> townLookup) {
    	this.geo = geo;
    	this.infra = infra;
    	this.transit = transit;
        this.budget = budget;
    }
    
    /**
     * Update ownerships and remaining budgets for all players.
     */
    public void updateStatus() {
    	// TODO
    }
    
    /**
     * Calculate the expected amount of traffic for each link in the infrastructure, for one specific player.
     * If an indirect route involves a link not owned by any player yet, the expectation assumes no 
     * switching penalty, but the revenue is divided by all possible routes proportional to distance 
     * of each route. Note that we will get fraction population number.
     * @param playerOwnedLinks a current or hypothetical map keeping track of ownership of each link
     * @param player the name of player
     * @return a map from each link denoted by an integer pair [i, j] to its corresponding traffic.
     * @see playerOwnedLinks
     */
    public Map<Pair, Double> getHeatMap(Map<Pair, String> playerOwnedLinks, String player) {
    	Map<Pair, Double> heatmap = new HashMap<Pair, Double>();
    	// TODO
    	return heatmap;
    }

    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids) {
    	// TODO Find a way such that we bid on a link that gives us 0 benefit externally (?)
    	// while giving other links that we owned higher traffics. I am not sure how to do this right now.
    	
    	// Random player code below
    	
    	// The random player bids only once in a round.
        // This checks whether we are in the same round.
        // Random player doesn't care about bids made by other players.
        if (availableBids.size() != 0) {
            return null;
        }

        for (BidInfo bi : allBids) {
            if (bi.owner == null) {
                availableBids.add(bi);
            }
        }

        if (availableBids.size() == 0) {
            return null;
        }

        BidInfo randomBid = availableBids.get(rand.nextInt(availableBids.size()));
        double amount = randomBid.amount;

        // Don't bid if the random bid turns out to be beyond our budget.
        if (budget - amount < 0.) {
            return null;
        }

        // Check if another player has made a bid for this link.
        for (Bid b : currentBids) {
            if (b.id1 == randomBid.id || b.id2 == randomBid.id) {
                if (budget - b.amount - 10000 < 0.) {
                    return null;
                }
                else {
                    amount = b.amount + 10000;
                }

                break;
            }
        }

        Bid bid = new Bid();
        bid.amount = amount;
        bid.id1 = randomBid.id;

        return bid;
    }

    public void updateBudget(Bid bid) {
    	// TODO
    	updateStatus();
        if (bid != null) {
            budget -= bid.amount;
        }

        availableBids = new ArrayList<>();
    }
}
