package railway.g4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.lang.Math;
// To access data classes.
import railway.sim.utils.*;

public class Player implements railway.sim.Player 
{

    private double budget;

    private List<BidInfo> availableBids = new ArrayList<>();
    
    // use maps to store each player's budget and links
    private HashMap<String, String> allPlayerLinks = new HashMap<>();
    
    private List<Coordinates> geo;
    private List<String> townLookup;
    private List<List<Integer>> infra;
    private int [][] transit;
    private double [][] flow;
    private int total_profit = 0; 
    private double total_budget = 0;
    private String name;
    private static int NUM_STATIONS;
    private ArrayList<Integer>[] adjList; 
    private HashMap<Integer, Coordinates> station_to_coordinates = new HashMap<Integer, Coordinates>();
    
    // For use with paths
    private ArrayList<Integer> pathList = new ArrayList<>(); 

    public Player() 
    {
    }

    public void init(
        String name,
        double budget,
        List<Coordinates> geo,
        List<List<Integer>> infra,
        int[][] transit,
        List<String> townLookup,
        List<BidInfo> allBids) 
    {
        this.name = name;
        this.townLookup = townLookup;
        this.geo = geo;
        this.budget = budget;
        this.infra = infra;
        this.transit = transit;
        this.total_budget = budget;
        this.NUM_STATIONS = geo.size();
        // Build a Naive List Containing all paths.
        // 1- Use DFS
        
        // With this, get the distances!
        
        // Sum up all transit links of path. Print Profit!
        
        // geography - Maps Station -. (x, y)
        // tranist maps (Station 1 -> Station 2, traffic)
        
        // Init Adjacency List
        adjList = new ArrayList[NUM_STATIONS];
        System.out.println("Number of Stations: " + NUM_STATIONS);
        for(int i = 0; i < NUM_STATIONS;i++)
        {
            adjList[i] = new ArrayList<>();
        }


        update_flow(); 
    }


    public void update_flow(){
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
        flow = new double[n][n];

        for (int i=0; i<n; ++i) {
            int[][] prev = Dijkstra.dijkstra(g, i);
            for (int j=i+1; j<n; ++j) {
                List<List<Integer>> allPaths = Dijkstra.getPaths(g, prev, j);

                for (int c = 0; c<allPaths.size();c++){
                    for (int k=0; k<allPaths.get(c).size()-1; ++k) {
                        flow[allPaths.get(c).get(k)][allPaths.get(c).get(k+1)] += transit[i][j]/allPaths.size();
                        flow[allPaths.get(c).get(k+1)][allPaths.get(c).get(k)] += transit[i][j]/allPaths.size();
                    }
                }
            }
        }
    }


    private double getDistance(Coordinates a, Coordinates b)
    {
        return Math.pow(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2), 0.5);
    }


    private double getDistance(int a, int b)
    {
        return Math.pow(Math.pow(geo.get(a).x - geo.get(b).x, 2) + Math.pow(geo.get(a).y - geo.get(b).y, 2), 0.5);
    }


    public Bid getBid(List<Bid> currentBids, List<BidInfo> availableBids, Bid lastRoundMaxBid) 
    {
        List<BidInfo> avail_Bids = new ArrayList<BidInfo>();
        List<Integer> amounts = new ArrayList<Integer>();

        // this update the flow every iteration
        update_flow();

        for (int i=0; i<availableBids.size();i++)
        {
            BidInfo bi = availableBids.get(i);
            if (bi.owner == null) 
            {
                avail_Bids.add(bi);
                amounts.add((int)(bi.amount+10000));
                // 
                int from = townLookup.indexOf(bi.town1);
                int to = townLookup.indexOf(bi.town2);   

                total_profit += flow[from][to]*20*getDistance(from,to) - bi.amount-10000;
            }
            else
            {
                amounts.add(-1);
            }
        }

        if (avail_Bids.size() == 0) 
        {
            return null;
        }
        
        double maxb = 0;
        Coordinates s = new Coordinates(0,0);
        Coordinates d = new Coordinates(0,0);
        String maxbn = "";
        // Check if another player has made a bid for this link.
        for (int i=0; i<currentBids.size();i++)
        {
            Bid b = currentBids.get(i);

            if (b.id2 == -1)
            {
                if (townLookup.indexOf(availableBids.get(b.id1).town1) == -1 || townLookup.indexOf(availableBids.get(b.id1).town2) == -1)
                {
                   continue;
                }
                s = geo.get(townLookup.indexOf(availableBids.get(b.id1).town1));
                d = geo.get(townLookup.indexOf(availableBids.get(b.id1).town2));

                double dist = getDistance(s,d);

                if (b.amount/dist > maxb)
                {
                   maxb = b.amount / dist;
                   maxbn = b.bidder;
                }
                if ((int)b.amount + 10000 > amounts.get(b.id1))
                {
                    amounts.set(b.id1,(int)b.amount + 10000);
                }
            }
            else
            {
                if (townLookup.indexOf(availableBids.get(b.id1).town1) == -1 || townLookup.indexOf(availableBids.get(b.id1).town2)==-1 || townLookup.indexOf(availableBids.get(b.id2).town1)==-1 || townLookup.indexOf(availableBids.get(b.id1).town2) == -1){
                   System.out.println("Town not found");
                   continue;
                }
                s = geo.get(townLookup.indexOf(availableBids.get(b.id1).town1));
                Coordinates t = geo.get(townLookup.indexOf(availableBids.get(b.id1).town2));
                Coordinates k = geo.get(townLookup.indexOf(availableBids.get(b.id2).town1));
                d = geo.get(townLookup.indexOf(availableBids.get(b.id2).town2));
                double dist1 = getDistance(s,t);
                double dist2 = getDistance(k,d);
                
                double dist = dist1+dist2;
                if (b.amount/dist > maxb)
                {
                   maxb = b.amount / dist;
                   maxbn = b.bidder;
                }

                if ((int)(dist1/dist*b.amount + 10000) > amounts.get(b.id1))
                {
                    amounts.set(b.id1,(int)(dist1/dist*b.amount) + 10000);
                }
                if ((int)(dist2/dist*b.amount + 10000) > amounts.get(b.id2))
                {
                    amounts.set(b.id2,(int)(dist2/dist*b.amount) + 10000);
                }
            }
        }
        
        if (maxbn.equals(name))
        {
            return null;
        }

        double profit = 0;
        Bid b = new Bid();
        
        for (int i=0; i<availableBids.size();i++)
        {
            BidInfo bi = availableBids.get(i);

            if (townLookup.indexOf(bi.town1) == -1 || townLookup.indexOf(bi.town2) == -1)
            {
               System.out.println("Town not found");
               continue;
            }
            int from = townLookup.indexOf(bi.town1);
            int to = townLookup.indexOf(bi.town2);
            if (amounts.get(i)==-1)
            {
                continue;
            }
            double win_bid = (double)(maxb*getDistance(geo.get(from),geo.get(to))+1);
            double winning_price = Math.max(amounts.get(i),win_bid);
            double revenue = flow[from][to]*20*getDistance(from,to);
/*            
            System.out.printf("from %d to %d revenue: %f\n",from, to, revenue);
            System.out.printf("price: %f budget: %f\n",winning_price, this.budget);
*/

            if (winning_price < revenue)
            {
                if (profit < revenue - winning_price)
                {
                    if (winning_price > budget )
                    {
                        continue;
                    }
                    
                    b.id1 = i;
                    b.amount = winning_price;
                    profit = revenue - winning_price;
                    //System.out.println(bi.town1+ " to "+bi.town2 + " : "+String.valueOf(profit));
                }
            }    
        }
        if (b.id1 == -1)
        {
            //System.out.println("Not worth it");
            //System.out.println(this.budget);
            
            return null;
        }
        return b;
    }


    public void updateBudget(Bid bid) 
    {
        if (bid != null) 
        {
            budget -= bid.amount;
        }
        availableBids = new ArrayList<>();
    }
    
    // determine if we want to keep bidding, 0 if not, 1 if yes
    public int stop_criterian(double cost, double revenue, double total_profit)
    {


        if (8*(revenue - cost)*this.total_budget < cost*total_profit)
        {
            return 0;
        }
        return 1;
    }

    
    /*
     *  Purpose: Find your starting point
     *  
     *  If graph has tail find a pair such that 
     *  degree of 1 is one station and highest degree in other station.
     */
    public void start()
    {
    	// Look for a station with degree 1.
    	Integer tail = null;
    	// If you find it, Great! Find a good neighbor with high order degree and spawn from there
    	if(tail == null)
    	{
    		
    	}
    	// Otherwise, you are in a checkerboard like map most likely so tails are liability!
    	else
    	{
    		
    	}
    }
}
