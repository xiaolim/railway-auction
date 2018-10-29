package railway.g5;

import java.util.HashMap;
import java.util.Map;
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

    private List<BidInfo> availableBids = new ArrayList<>();

    private Map<Integer, Integer> railValues = new HashMap<Integer, Integer>();

    public Player() {
        rand = new Random();
    }

    public void init(
        String name,
        double budget,
        List<Coordinates> geo,
        List<List<Integer>> infra,
        int[][] transit,
        List<String> townLookup) {
	
        this.budget = budget;

	
	Map<Integer, Integer> cityTraffic = new HashMap<Integer, Integer>();

	for (int m=0; m<geo.size(); m++){
	    cityTraffic.put(m, 0);
	    for (int n=0; n<geo.size(); n++){
		if( n == m ) {
		    for(int p=0; p<geo.size(); p++){
			cityTraffic.put(m, cityTraffic.get(m)+transit[n][p]);
		    }
		} else {
		    cityTraffic.put(m, cityTraffic.get(m)+transit[n][m]);
		}
	    }
	}

	int id = 0;

	for (int i=0; i<infra.size(); ++i){
	    for (int j=0; j<infra.get(i).size(); ++j){
		int irails = infra.get(i).size();
		int jrails = infra.get(infra.get(i).get(j)).size();
		
		//		System.out.printf("i is %d, irails starting at %d\n", i, irails);
		//		System.out.printf("j is %d, jrails starting at %d\n", j, jrails);
		

		for (int k=0; k<infra.size(); ++k){
		    if (infra.get(k).contains(i) ){
			irails +=1;
			//	System.out.printf("found a %d in line %d, irails now %d\n", i, k, irails);
		    }
		    if (infra.get(k).contains(infra.get(i).get(j)) ){
			jrails +=1;
			//	System.out.printf("found a %d in line %d, jrails now %d\n", j, k, jrails);
		    }
		}
		int value = 0;
		if (irails != 0){
		    value += cityTraffic.get(i)/irails;
		}
		if (jrails != 0){
		    value += cityTraffic.get(j)/jrails;
		}
		railValues.put(id, value);
		id++;
	    }
	}
    }

    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids) {
        // Check if everyone else has dropped out

        // Sort through the bids to find the current winning bid

        // Find a rail we would like to buy past that price / distance

        // Make winning bid


        // Sort though currentBids to make find rails that aren't bet on yet

        // Choose one and make a minimum bid


        // If there are no rails we'd like to purchase at the highest price / distance
        // Drop out


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
        if (bid != null) {
            budget -= bid.amount;
        }

        availableBids = new ArrayList<>();
    }
}
