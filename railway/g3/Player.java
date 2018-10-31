package railway.g3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// To access data classes.
import railway.sim.utils.*;

public class Player implements railway.sim.Player {
    // Random seed of 42.
    // private int seed = 42;
    // private Random rand;

    private double budget; // amount to spend at auction

    private List<Coordinates> geo; // city locations
    private ArrayList<String> townLookup; // city names
    private List<List<Integer>> infra; // graph edges
    private int[][] transit; // travel along paths
    
    //private BidInfo the_bid;

    private double[][] revenue; // price to travel along paths
    private int[] connections; // number of connections for links

    private boolean[][] ownership; // which links we own
    private List<G3Bid> availableBids = new ArrayList<G3Bid>();

    public Player() {
        //rand = new Random();
    }

    public void init(
        String name,
        double budget,
        List<Coordinates> geo,
        List<List<Integer>> infra,
        int[][] transit,
        List<String> townLookup,
        List<BidInfo> allBids) {

    	System.err.println("Starting");

        this.budget = budget;
        this.townLookup = (ArrayList)townLookup;
        this.infra = infra;
        this.geo = geo;
        this.transit = transit;
    	this.revenue = getRevenue();
    	this.connections = getConnections();

	    //ownership = new boolean[transit.length][transit[0].length];

        System.err.println("Ending");

    }

    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids, Bid lastRoundMaxBid) {
        // The random player bids only once in a round.
        // This checks whether we are in the same round.
        // Random player doesn't care about bids made by other players.
        // if (availableBids.size() != 0) {
        //     return null;
        // }

        for (BidInfo bi : allBids) {
            if (bi.owner == null) {
                availableBids.add(bi);
            }
        }

        if (availableBids.size() == 0) {
            return null;
        }

        // int max_connections = -1;
        // BidInfo max_bid = null;
        // for (BidInfo cur_bid : availableBids) {
        //     String t1 = cur_bid.town1;
        //     String t2 = cur_bid.town2;
        //     int t1_i = townLookup.indexOf(t1);
        //     int t2_i = townLookup.indexOf(t2);

        //     int num_connections = connections[t1_i] + connections[t2_i];
        //     if (num_connections > max_connections) {
        //         max_connections = num_connections;
        //         max_bid = cur_bid;
        //     }

        // }
        
        //double amount = max_bid.amount;

        double max_profit = 0;
        G3Bid best = null;
        
        for(G3Bid cur_bid : availableBids) {
        	if (cur_bid.town_id3 == -1) { 
        		// check single links
        		int t1_i = cur_bid.town_id1;
            	int t2_i = cur_bid.town_id2;
            	double cur_rev = (t1_i < t2_i) ? (revenue[t1_i][t2_i]) : (revenue[t2_i][t1_i]);
        		double cur_profit = cur_bid.amount - cur_rev;

	        	if (cur_profit >= max_profit) {
	        		max_profit = cur_profit;
	        		best = cur_bid;
	        	}
        	} else {
        		// ignore double links for now
        	}
            
            
        }

        // Don't bid if it's more profitable not to buy, or we don't have enough to afford
        if (best == null || budget - best.amount < 0.) { 
            return null;
        }

        return best;
    }

    // Thanks Sidhi!
    private double[][] getRevenue() {
        final int n = geo.size();

        // Create the graph.
        WeightedGraph g = new WeightedGraph(n);

        for (int i=0; i<n; ++i) {
            g.setLabel(townLookup.get(i));
        }

        for (int i=0; i<infra.size(); ++i) {
            for (int j=0; j<infra.get(i).size(); ++j) {
                g.addEdge(i, infra.get(i).get(j), getDistance(i, infra.get(i).get(j)));
            }
        }

        //g.print();

        // Compute the revenue between any two nodes.
        double[][] revenue = new double[n][n];

        for (int i=0; i<n; ++i) {
            int[][] prev = Dijkstra.dijkstra(g, i);
            for (int j=i+1; j<n; ++j) {
                List<List<Integer>> allPaths = Dijkstra.getPaths(g, prev, j);

                double cost = 0;
                for (int k=0; k<allPaths.get(0).size()-1; ++k) {
                    cost += getDistance(allPaths.get(0).get(k), allPaths.get(0).get(k+1));
                }

                revenue[i][j] = cost * transit[i][j] * 10;
            }
        }

        return revenue;
    }

    // Thanks Sidhi!
    private double getDistance(int t1, int t2) {
        return Math.pow(
            Math.pow(geo.get(t1).x - geo.get(t2).x, 2) +
                Math.pow(geo.get(t1).y - geo.get(t2).y, 2),
            0.5);
    }

    private int[] getConnections() {
    	int[] connections = new int[geo.size()];
	    for(int i = 0; i < connections.length; ++i) {
	        List<Integer> row = infra.get(i);
	        connections[i] += row.size();
	        for(int j = 0; j < row.size(); ++j) {
	            ++connections[row.get(j)];
	        }
	    }

	    return connections;
    }

    public void updateBudget(Bid bid) {
        if (bid != null) {
            budget -= bid.amount;
        }

        availableBids = new ArrayList<>();
    }
}
