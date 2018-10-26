package railway.g8;

import railway.sim.utils.Bid;
import railway.sim.utils.BidInfo;
import railway.sim.utils.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays; 
import javafx.util.Pair;
import java.util.HashMap; 


class Connection{
    int row; 
    int column; 

    public Connection(int row, int column){
        this.row = row; 
        this.column = column; 
    }
}

public class Player implements railway.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;

    private double budget;

    private List<BidInfo> availableBids = new ArrayList<>();

    private List<List<Integer>> infra; 
    private int[][] transit; 

    //hashmap of all our connections- and then map connection 1 etc. to amount of traffic between 
    private HashMap<Connection, Integer> connections = new HashMap<Connection, Integer>(); 

    public Player() {
        rand = new Random();
    }

    public void init(

        String name,
        double budget,
        List<Coordinates> geo,
        List<List<Integer>> infra, //
        int[][] transit, //
        List<String> townLookup) {

        this.budget = budget;
        this.transit = transit; 
        this.infra = infra; 

        buildHashMap(); 
    }


    private void calculateHighestTraffic(){

    }

    private void buildHashMap(){
        for(int l = 0; l < infra.size(); l++){
            List<Integer> row = infra.get(l); 
            for(int i = 0; i < row.size(); i++){
                int there = row.get(i); 
                Connection pair = new Connection(l,there); 
                int traffic = transit[l][there]; 
                connections.put(pair, traffic); 
            }
        }
    }

    private void printRow(int[] row) {
        for (int i : row) {
            System.out.print(i);
            System.out.print("\t");
        }
        System.out.println();
    }


    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids) {
        // The random player bids only once in a round.
        // This checks whether we are in the same round.
        // Random player doesn't care about bids made by other players.
        System.out.println("This is our budget: " + budget); 
        System.out.println("This is the infra: " + infra); 
        

        System.out.println("This is the transit: "); 
        for(int[] row : transit) {
            printRow(row);
        }


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
                } else {
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
