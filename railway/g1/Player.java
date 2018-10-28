package railway.g1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import railway.sim.utils.*;
// To access data classes.


public class Player implements railway.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand = new Random(seed);
    private double[][] min_price; //minimum price to buy  link i,j
    private double[][] revenue; //revenue of link i,j
    private int[][] min_path; //record whether link i-j is a minimum path from i to j
    private double budget;
    private Map<String,Integer> map; // this map is used to query index of town
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


    private double getDistance(int t1, int t2) {
        return Math.pow(
                Math.pow(geo.get(t1).x - geo.get(t2).x, 2) +
                        Math.pow(geo.get(t1).y - geo.get(t2).y, 2),
                0.5);
    }

    /**
     * Thie function will generate a revenue matrix for the problem
     * It will use dijkstra method to find the shortest path for i th node to j th node
     * And store the result in revenue[i][j], also this will give us the minimum price to buy links
     * @return the revenur matrix
     */
    private double[][] getRevenue() {
        int n = geo.size();
        // Create the graph.
        WeightedGraph g = new WeightedGraph(n);
        for (int i=0; i<infra.size(); ++i) {
            for (int j=0; j<infra.get(i).size(); ++j) {
                g.addEdge(i, infra.get(i).get(j), getDistance(i, infra.get(i).get(j)));
            }
        }
       // System.out.println("tag2");
        // Compute the revenue between any two nodes.
        revenue = new double[n][n];
        min_price = new double[n][n];
        min_path =  new int[n][n];
        for (int i=0; i<n; ++i) {
            int[][] prev = Dijkstra.dijkstra(g, i);
            for (int j=i+1; j<n; ++j) {
                List<List<Integer>> allPaths = Dijkstra.getPaths(g, prev, j);
                System.out.println("tag3"+ j +i + n);
                double cost = 0;
                for (int k=0; k<allPaths.get(0).size()-1; ++k) {
                    cost += getDistance(allPaths.get(0).get(k), allPaths.get(0).get(k+1));
                }

                for (int k=0; k<allPaths.size(); ++k) {
                    //System.out.println("tag4"+" "+allPaths.get(k).get(0)+" "+allPaths.get(k).get(allPaths.get(0).size()-1));
                    min_path[allPaths.get(k).get(0)][allPaths.get(k).get(allPaths.get(0).size()-1)] = 1;            //record minimum path here
                    //System.out.println("tag6");
                    min_path[allPaths.get(k).get(allPaths.get(0).size()-1)][allPaths.get(k).get(0)] = 1;            //record minimum path here
                }
                System.out.println("tag5");
                revenue[i][j] = cost * transit[i][j] * 10;
                min_price[i][j] = revenue[i][j]/allPaths.size();
            }
        }
        return revenue;
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
        //System.out.println("tag1");
        this.revenue = getRevenue();
        //System.out.println("tag2");
        map = new HashMap();
        //System.out.println("tag3");
        for(int i=0;i<townLookup.size();i++)
            map.put(townLookup.get(i),i);
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
    //This is to Query the info of according bid
    private BidInfo QBidInfo(Bid bid, List<BidInfo> allBids){
        for(BidInfo bi : allBids){
            if(bi.id == bid.id1){
                return bi;
            }
        }
        return null;
    }
    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids) {
    	// TODO Find a way such that we bid on a link that gives us 0 benefit externally (?)
    	// while giving other links that we owned higher traffics. I am not sure how to do this right now.
    	
    	// Random player code below
    	
    	// The random player bids only once in a round.
        // This checks whether we are in the same round.
        // Random player doesn't care about bids made by other players.
        double amount = -1;
        int id = -1;
        double maxamount = amount;
        int maxid = id;
        if (availableBids.size() != 0) {
            return null;
        }
        List<BidInfo> ourlinks = new ArrayList<>();
        for(BidInfo bi : allBids){
            if (bi.owner == "g1"){
                ourlinks.add(bi);
            }
        }
        for (BidInfo bi : allBids) {

            if (bi.owner == null) {
                if(ourlinks.size()==0){
                    if(min_path[map.get(bi.town1)][map.get(bi.town2)]!=1){
                        continue;
                    }
                }      //we will start with a shortest link
                availableBids.add(bi);
                amount = revenue[map.get(bi.town1)][map.get(bi.town2)];
                for(BidInfo bi1 : ourlinks){
                    if(bi1.town1.equals(bi.town1)){
                        if(min_path[map.get(bi1.town2)][map.get(bi.town2)]==1){
                            amount = amount + min_price[map.get(bi1.town2)][map.get(bi.town2)];
                            continue;
                        }
                    }
                    if(bi1.town2.equals(bi.town1)){
                        if(min_path[map.get(bi1.town1)][map.get(bi.town2)]==1){
                            amount = amount + min_price[map.get(bi1.town1)][map.get(bi.town2)];
                            continue;
                        }

                    }
                    if(bi1.town1.equals(bi.town2)){
                        if(min_path[map.get(bi1.town2)][map.get(bi.town1)]==1){
                            amount = amount + min_price[map.get(bi1.town2)][map.get(bi.town1)];
                            continue;
                        }

                    }
                    if(bi1.town2.equals(bi.town2)){
                        if(min_path[map.get(bi1.town1)][map.get(bi.town1)]==1){
                            amount = amount + min_price[map.get(bi1.town1)][map.get(bi.town1)];
                            continue;
                        }

                    }
                }
                if(amount > maxamount){
                    maxamount = amount;
                    maxid = bi.id;
                }

            }


        }
        System.out.println(maxamount+" "+maxid);
        if (availableBids.size() == 0) {
            return null;
        }



//        BidInfo randomBid = availableBids.get(rand.nextInt(availableBids.size()));

        // Don't bid if the random bid turns out to be beyond our budget.
//        if (budget - amount < 0.) {
//            return null;
//        }

        // Check if another player has made a bid for this link.
//
//        for (BidInfo bi : currentBids) {
//
//        }

        Bid bid = new Bid();
        bid.amount = maxamount;
        bid.id1 = maxid;

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
