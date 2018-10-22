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
        this.timer = new Timer();
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

    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids) throws Exception {
        Log.record("Getting bid for player " + this.name);

        Bid bid;

        try {
            if (!timer.isAlive()) timer.start();

            timer.call_start(new Callable<Bid>() {
                @Override
                public Bid call() throws Exception {
                    return player.getBid(currentBids, allBids);
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

        if (!isValid(bid, currentBids, allBids)) {
            throw new IllegalArgumentException("The player " + this.name + " has selected " +
                "an invalid link.");
        }

        bid.bidder = this.name;
        return bid;
    }

    public void updateBudget(Bid bid) {
        if (bid != null) {
            this.budget -= bid.amount;
        }

        this.player.updateBudget(bid);
    }

    public String getName() {
        return name;
    }

    public double getBudget() {
        return budget;
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

    private boolean isValid(Bid bid, List<Bid> currentBids, List<BidInfo> allBids) {
        boolean id1_correct = false, id2_correct = false;
        double amount = 0.;

        String t1a = "", t1b = "";
        String t2a = "", t2b = "";

        // The player is bidding for the same link twice in their link pair.
        if (bid.id1 == bid.id2) {
            return false;
        }

        for (Bid b : currentBids) {
            if (b.id1 == bid.id1 && b.id2 == bid.id2) {
                if (b.amount + 10000 > bid.amount) {
                    // The player has not increased the bid by 10000.
                    return false;
                }
            }
        }

        for (BidInfo bi : allBids) {
            if (bi.id == bid.id1) {
                if (bi.owner == null) {
                    id1_correct = true;
                    amount += bi.amount;
                    t1a = bi.town1;
                    t1b = bi.town2;
                }
                else {
                    // The bid is for an owned link.
                    return false;
                }
            }
            else if (bi.id == bid.id2) {
                if (bi.owner == null) {
                    id2_correct = true;
                    amount += bi.amount;
                    t2a = bi.town1;
                    t2b = bi.town2;
                }
                else {
                    // The bid is for an owned link.
                    return false;
                }
            }
        }

        if (!(id1_correct && (bid.id2 == -1 || id2_correct))) {
            return false;
        }

        // Check if player is bidding less that the minimum amount.
        if (bid.amount < amount) {
            return false;
        }

        for (BidInfo bi : allBids) {
            // Check if player is bidding for a link between two towns when they already
            //  own one of the links.
            if (bi.id != bid.id1 && bi.town1.equals(t1a) && bi.town2.equals(t1b) &&
                bi.owner.equals(this.getName())) {
                    return false;
            }

            if (bi.id != bid.id2 && bi.town1.equals(t2a) && bi.town2.equals(t2b) &&
                bi.owner.equals(this.getName())) {
                    return false;
            }
        }

        // Check if player is bidding for multiple links between the same towns.
        if (t1a.equals(t2a) && t1b.equals(t2b)) {
            return false;
        }

        // Check if there is atleast one town in common when the player is requesting
        //  for a pair of nodes.
        if (bid.id2 != -1) {
            if (!t1a.equals(t2a) && !t1a.equals(t2b) && !t1b.equals(t2a) && !t1b.equals(t2b)) {
                return false;
            }
        }

        return true;
    }
}
