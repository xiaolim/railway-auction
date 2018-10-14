package railway.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.List;

import railway.sim.utils.*;

public class PlayerWrapper {
    private Timer timer;
    private Player player;
    private boolean isHome;
    private String name;

    private double budget;
    private long timeout;

    private List<List<Integer>> distribution;
    private List<Integer> skills;

    private List<Integer> playedRows;

    private List<String> linkNames;

    public PlayerWrapper(Player player, String name, long timeout) {
        this.player = player;
        this.name = name;
        this.timeout = timeout;
    }

    public void init(
        double budget,
        List<Coordinates> geo,
        List<List<Integer>> infra,
        int[][] transit,
        List<String> townLookup) throws Exception {
        
        Log.record("Initializing player " + this.name);
        
        this.budget = budget;
        this.player.init(this.name, budget, geo, infra, transit, townLookup);
    }

    public Bid getBid(List<BidInfo> allBids) throws Exception {
        Log.record("Getting bid for player " + this.name);

        Bid bid;

        try {
            if (!timer.isAlive()) timer.start();
            
            timer.call_start(new Callable<Bid>() {
                @Override
                public Bid call() throws Exception {
                    return player.getBid(allBids);
                }
            });

            bid = timer.call_wait(timeout);
        }
        catch (Exception ex) {
            System.out.println("Player " + this.name + " has possibly timed out.");
            throw ex;
        }

        if (bid == null) {
            System.out.println("Player " + this.name + " did not bid.");
            return null;
        }

        System.out.println("Player " + this.name + " bid " + bid.amount + " for link " +
            getLinkString(bid, allBids));

        if (budget - bid.amount < 0.) {
            throw new IllegalArgumentException("The player " + this.name + " is requesting " +
                "out of their budget.");
        }

        if (!isValid(bid, allBids)) {
            throw new IllegalArgumentException("The player " + this.name + " has selected " +
                "an invalid link.");
        }

        return bid;
    }

    public void updateBudget(int id1, int id2, double amount) {
        this.player.updateBudget(id1, id2, amount);
    }

    public String getName() {
        return name;
    }

    private String getLinkString(Bid bid, List<BidInfo> allBids) {
        if (linkNames == null || linkNames.size() == 0) {
            linkNames = new ArrayList<>(allBids.size());

            for (BidInfo bi : allBids) {
                linkNames.add(bi.id, bi.town1 + "-" + bi.town2);
            }
        }

        String linkString = "";
        if (bid.id1 != -1) {
            linkString += linkNames.get(bid.id1);
        }

        if (bid.id2 != -1) {
            linkString += " " + linkNames.get(bid.id2);
        }

        return linkString;
    }

    private boolean isValid(Bid bid, List<BidInfo> allBids) {
        boolean id1_correct = false, id2_correct = false;

        for (BidInfo bi : allBids) { 
            if (bi.id == bid.id1) {
                if (bi.owner == null) {
                    id1_correct = true;
                }

                // The bid is for an owned link.
                return false;
            }
            else if (bi.id == bid.id2) {
                if (bi.owner == null) {
                    id2_correct = true;
                }

                // The bid is for an owned link.
                return false;
            }

        }

        return id1_correct && (bid.id2 == -1 || id2_correct);
    }
}
