package railway.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.List;

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

    public PlayerWrapper(Player player, String name, long timeout) {
        this.player = player;
        this.name = name;
        this.timeout = timeout;
    }

    public void init(String name, double budget) throws Exception {
        Log.record("Initializing player " + this.name);
        
        this.budget = budget;
        this.player.init(budget);
    }

    public Bid getBid(List<BidInfo> allBids) throws Exception {
        Log.record("Getting bid for player " + this.name);

        Bid bid;

        try {
            if (!timer.isAlive()) timer.start();
            
            timer.call_start(new Callable<Bid>() {
                @Override
                public Bid call() throws Exception {
                    return this.player.getBid(allBids);
                }
            });

            bid = timer.call_wait(timeout);
        }
        catch (Exception ex) {
            System.out.println("Player " + this.name + " has possibly timed out.");
            throw;
        }

        System.out.println("Player " + this.name + " bid " + bid.getValue() + " for link " +
            bid.getKey() + ".");

        budget -= bid.getValue();
        if (budget < 0.) {
            throw new IllegalArgumentException("The player " + this.name + " has exceeded " +
                "their budget.");
        }

        if (!isValidId(bid.id, allBids)) {
            throw new IllegalArgumentException("The player " + this.name + " has selected " +
                "an invalid link.");
        }

        return bid;
    }

    public String getName() {
        return name;
    }

    private boolean isValidId(int id, List<BidInfo> allBids) {
        for (BidInfo bi : allBids) {
            if (bi.id == id) {
                return true;
            }
        }

        return false;
    }
}
