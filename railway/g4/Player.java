package railway.g4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

// To access data classes.
import railway.sim.utils.*;

public class Player implements railway.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;

    private double budget;

    private List<BidInfo> availableBids = new ArrayList<>();
    
    // use maps to store each player's budget and arrays for links
    private HashMap<String, Double> allPlayerBudget = new HashMap<>();

    private HashMap<String, LinkedList<String>> allPlayerLinks = new HashMap<>();

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

        for (int i = 1; i<=8; i++) {
            String group = "g" + Integer.toString(i);
            allPlayerBudget.put(group, budget);
        }
    }

    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids) {
        // The random player bids only once in a round.
        // This checks whether we are in the same round.
        // Random player doesn't care about bids made by other players.
        if (availableBids.size() != 0) {
            return null;
        }

        LinkedList<String> links = new LinkedList<>();

        // System.out.println("all taken bids: ");
        for (BidInfo b : allBids) {
            if (b.owner != null) {
                String link = b.town1 + "-" + b.town2;
                // System.out.println("id: " + b.id);
                // System.out.println("link: " + link);
                // System.out.println("owner: " + b.owner);
                // System.out.println("amount paid: " + b.amount);

                // check if link is in allPlayerLinks. If not, put it in and update budget
                if (allPlayerLinks.get(b.owner) == null) {
                    Double oldBudget = allPlayerBudget.get(b.owner);
                    Double newBudget = oldBudget - b.amount;
                    allPlayerBudget.put(b.owner, newBudget);
                    //put link in
                    links.add(link);
                    allPlayerLinks.put(b.owner, links);

                }
                else {
                    LinkedList<String> playerLinks = allPlayerLinks.get(b.owner);
                    if (!playerLinks.contains(link)) {
                        playerLinks.add(link);
                        //update budget
                        Double oldBudget = allPlayerBudget.get(b.owner);
                        Double newBudget = oldBudget - b.amount;
                        allPlayerBudget.put(b.owner, newBudget);
                    }
                }
            }
        }

        System.out.println("taken links: ");
        for (Map.Entry<String, LinkedList<String>> entry : allPlayerLinks.entrySet() ) {
            String key = entry.getKey();
            LinkedList<String> value = entry.getValue();
            System.out.print(key + ": " + value);
            System.out.println();
            
        }  

        System.out.println("group budget: ");
        for (Map.Entry<String, Double> entry : allPlayerBudget.entrySet() ) {
            String key = entry.getKey();
            Double value = entry.getValue();
            System.out.print(key + ": ");
            System.out.println(value);
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
