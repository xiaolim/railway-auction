package railway.g3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


// To access data classes.
import railway.sim.utils.*;

// Thanks Sidhi!
class LinkInfo {
    public int town1;
    public int town2;

    public double distance;
}

public class Player implements railway.sim.Player {
    // Random seed of 42.
    // private int seed = 42;
    // private Random rand;

    private double budget; // amount to spend at auction

    private List<Coordinates> geo; // city locations
    private ArrayList<String> townLookup; // city names
    private List<List<Integer>> infra; // graph edges
    private int[][] transit; // travel along paths

    private double[][] revenue; // price to travel along paths
    private int[] connections; // number of connections for links

    private boolean[][] ownership; // which links we own
    private List<G3Bid> availableBids = new ArrayList<G3Bid>();

    

    public Player() {
        //rand = new Random();
    }

    // Initialization function.
    // name: name of current player.
    // budget: maximum amount allocated for bidding.
    // geo: Town indices and their coordinates.
    // infra: Town indices and other towns they are connected to.
    // transit: Town indices and the number of passengers between them.
    // townLookup: Town indices and names of towns.
    // allBids: list of all available bids and their minimum amount.
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

        // Populate bid container data structure to hold all of our team's possible bids
        //check running time...!!!
        //also keep track of current bid that has been made on some link?

        //HAVE TO MAKE SURE THAT "DIRECTIONALITY" OF LINKS DOES NOT MAKE A DIFFERENCE BUT MAKE SURE THERE ARE NO REPETITIONS...
        //e.g. we need Chate-Michel-Invalid but don't want to also include Invalid-Michel-Chate
        //so...this should be okay actually....
        //maybe do some calculations to make sure that we have the correct total number of links
        //test on simpler maps (like the random one, and like the random one with added complexity)

        //maybe include a check to make sure t3!=t2 and t3!=t1 but why are we getting some links where this is true in the first place?
        //think I fixed this duplicate problem above by making sure that I wasn't confusing indices with town id's
        //in the for loops, i is the only index that corresponds to a town id

        int id = 0;

        for (int i=0; i < infra.size(); ++i) {
            for (int j=0; j < infra.get(i).size(); ++j) {
                G3Bid bi = new G3Bid();
                bi.id1 = id;

                int t1 = i;
                int t2 = infra.get(i).get(j);
                bi.town_id1 = t1;
                bi.town_id2 = t2;
                //System.err.println(bi.town1 + ": infra.get(i) size = " + infra.get(i).size());

                bi.amount = transit[t1][t2] * getDistance(t1, t2) * 10;

                List<G3Bid> dups =
                    getDuplicateLinks(t1, t2, availableBids);
                if (dups != null && dups.size() > 0) {
                    //System.err.println("duplicate! >> " + dups.get(0).id);
                    int c_size = dups.size();
                    int new_size = c_size + 1;

                    for (G3Bid d : dups) {
                        d.amount = d.amount * c_size / new_size;
                    }

                    bi.amount /= new_size;
                }

                System.out.println("from 1st >> " + bi.id1 + ": " + townLookup.get(bi.town_id1) + " to " + townLookup.get(bi.town_id2));
                availableBids.add(bi);
                id += 1;

                // Create a new bid object for all possible pairs that contain this link between A and B and then all possible links between B and its neighbors
                for (int k=0; k < infra.get(t2).size(); ++k) {
                    G3Bid pairBid = new G3Bid();
                    pairBid.id1 = id;
                    pairBid.town_id1 = bi.town_id1;
                    pairBid.town_id2 = bi.town_id2;
                    pairBid.town_id3 = infra.get(t2).get(k);

                    // if these three towns are form a tricycle, then handle all 3 pair-links that can be formed from these towns
                    if (isTricycle(pairBid.town_id1, pairBid.town_id2, pairBid.town_id3) && (!availableBids.contains(pairBid))) {
                        System.out.println("from 2nd (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                        availableBids.add(pairBid);
                        id += 1;

                        pairBid = new G3Bid();
                        pairBid.id1 = id;
                        pairBid.town_id1 = infra.get(t2).get(k);
                        pairBid.town_id2 = bi.town_id1;
                        pairBid.town_id3 = bi.town_id2;
                        System.out.println("from 2nd (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                        availableBids.add(pairBid);
                        id += 1;
                        
                        pairBid = new G3Bid();
                        pairBid.id1 = id;
                        pairBid.town_id1 = bi.town_id2;
                        pairBid.town_id2 = infra.get(t2).get(k);
                        pairBid.town_id3 = bi.town_id1;
                        System.out.println("from 2nd (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                        availableBids.add(pairBid);
                        id += 1;
                        
                    } else {
                        if (!availableBids.contains(pairBid)) {
                            System.out.println("from 2nd >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                            availableBids.add(pairBid);
                            id += 1;
                        }
                    }
                    
                }

                // Also search for the neighbors of B which contain B in their infra list (infra file contains C-B but not B-C)
                for (int c=0; c < infra.size(); ++c) {
                    if (c != bi.town_id1 && infra.get(c).contains(bi.town_id2)) {
                        G3Bid pairBid = new G3Bid();
                        pairBid.id1 = id;
                        pairBid.town_id1 = bi.town_id1;
                        pairBid.town_id2 = bi.town_id2;
                        pairBid.town_id3 = c;
                        // if these three towns are form a tricycle, then handle all 3 pair-links that can be formed from these towns
                        if (isTricycle(pairBid.town_id1, pairBid.town_id2, pairBid.town_id3) && (!availableBids.contains(pairBid))) {
                            System.out.println("from 3rd (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                            availableBids.add(pairBid);
                            id += 1;

                            pairBid = new G3Bid();
                            pairBid.id1 = id;
                            pairBid.town_id1 = c;
                            pairBid.town_id2 = bi.town_id1;
                            pairBid.town_id3 = bi.town_id2;
                            System.out.println("from 3rd (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                            availableBids.add(pairBid);
                            id += 1;
                            
                            pairBid = new G3Bid();
                            pairBid.id1 = id;
                            pairBid.town_id1 = bi.town_id2;
                            pairBid.town_id2 = c;
                            pairBid.town_id3 = bi.town_id1;
                            System.out.println("from 3rd (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                            availableBids.add(pairBid);
                            id += 1;
                            
                        } else {
                            if (!availableBids.contains(pairBid)) {
                                System.out.println("from 3rd >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                                availableBids.add(pairBid);
                                id += 1;
                            }
                        }
                    }
                }

                // Also search for neighbors of A which are connected to some other link and link to B
                System.err.println("town i: " + townLookup.get(i));
                System.err.println("neighbor B: " + townLookup.get(bi.town_id2));
                for (int z=0; z < infra.get(bi.town_id1).size(); ++z) {
                    System.err.println("neighbor Z: " + townLookup.get(infra.get(bi.town_id1).get(z)));
                    if (infra.get(bi.town_id1).get(z) != bi.town_id2) {
                        G3Bid pairBid = new G3Bid();
                        pairBid.id1 = id;
                        pairBid.town_id1 = infra.get(bi.town_id1).get(z);
                        pairBid.town_id2 = bi.town_id1;
                        pairBid.town_id3 = bi.town_id2;
                        // if these three towns are form a tricycle, then handle all 3 pair-links that can be formed from these towns
                        if (isTricycle(pairBid.town_id1, pairBid.town_id2, pairBid.town_id3) && (!availableBids.contains(pairBid))) {
                            System.out.println("from 4th (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                            availableBids.add(pairBid);
                            id += 1;

                            pairBid = new G3Bid();
                            pairBid.id1 = id;
                            pairBid.town_id1 = bi.town_id2;
                            pairBid.town_id2 = bi.town_id1;
                            pairBid.town_id3 = infra.get(bi.town_id1).get(z);
                            System.out.println("from 4th (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                            availableBids.add(pairBid);
                            id += 1;
                            
                            pairBid = new G3Bid();
                            pairBid.id1 = id;
                            pairBid.town_id1 = bi.town_id2;
                            pairBid.town_id2 = infra.get(bi.town_id1).get(z);
                            pairBid.town_id3 = bi.town_id1;
                            System.out.println("from 4th (cycle) >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                            availableBids.add(pairBid);
                            id += 1;
                            
                        } else {
                            if (!availableBids.contains(pairBid)) {
                                System.out.println("from 4th >> " + pairBid.id1 + ": " + townLookup.get(pairBid.town_id1) + " to " + townLookup.get(pairBid.town_id2) + " to " + townLookup.get(pairBid.town_id3));
                                availableBids.add(pairBid);
                                id += 1;
                            }
                        }
                    }
                }
                // we do not want triplet links with the same 3 towns UNLESS they form a cycle
            }
        }

        System.err.println("bids in availableBids");
        for (G3Bid b : availableBids) {
            int t_1 = b.town_id1;
            int t_2 = b.town_id2;
            if (b.town_id3 > -1) {
                int t_3 = b.town_id3;
                System.err.println(b.id1 + ": " + townLookup.get(t_1) + " to " + townLookup.get(t_2) + " to " + townLookup.get(t_3));
            } else {
                System.err.println(b.id1 + ": " + townLookup.get(t_1) + " to " + townLookup.get(t_2));
            }
        }

        System.err.println("testing error in number of bids");
        for (G3Bid b : availableBids) {
            int t_1 = b.town_id1;
            int t_2 = b.town_id2;
            if (b.town_id3 > -1) {
                int t_3 = b.town_id3;
                if (isTricycle(t_1, t_2, t_3)) {
                    System.out.println("CYCLE!!! : " + townLookup.get(t_1) + ", " + townLookup.get(t_2) + ", " + townLookup.get(t_3));
                }
            }
        }

        System.err.println("Ending");
    }


    /* First version:
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

    }*/

    // The bid placed for a round.
    // Returns null if they don't want to place a bid.
    // currentBids: bids being placed in this round in reverse order -
    //   most recent bid is first.
    // allBids: shows those bids available and bids owned by other players
    //   i.e. the results of previous rounds.
    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids, Bid lastRoundMaxBid) {

        System.err.println("!!not doing anything!!");

        return null;

        /*for (BidInfo bi : allBids) {
            if (bi.owner == null) {
                //...
            }
        }*/


    }

    /* First version:
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
    }*/

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

    private static List<G3Bid> getDuplicateLinks(int t1, int t2, List<G3Bid> allBids) {
        List<G3Bid> dups = new ArrayList<>();
        for (G3Bid a : allBids) {
            if (a.town_id1 == t1 && a.town_id2 == t2) {
                dups.add(a);
            }
        }

        return dups;
    }

    private boolean isTricycle(int t1, int t2, int t3) {
        // checks if these three cities form a cycle
        if (infra.get(t1).contains(t2) && infra.get(t1).contains(t3) && (infra.get(t2).contains(t3) || infra.get(t3).contains(t2))) {
            return true;
        }
        if (infra.get(t2).contains(t1) && infra.get(t2).contains(t3) && (infra.get(t1).contains(t3) || infra.get(t3).contains(t1))) {
            return true;
        }
        if (infra.get(t3).contains(t1) && infra.get(t3).contains(t2) && (infra.get(t1).contains(t2) || infra.get(t2).contains(t1))) {
            return true;
        }
        return false;

    }

    // Indicates to the player whether they have won the previous bid of link/pair of links.
    // A null bid indicates that they did not win their bid.
    public void updateBudget(Bid bid) {
        if (bid != null) {
            budget -= bid.amount;
        }
    }
}