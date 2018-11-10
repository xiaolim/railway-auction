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
    private HashMap<Integer, Integer> Revenue = new HashMap<Integer, Integer>();
    private List<Coordinates> geo;
    private List<String> townLookup;
    private List<List<Integer>> infra;
    private int [][] transit;
    private String [][] owner;
    private int total_profit = 0; 
    private double total_budget = 0;
    private String name;
    private static int NUM_STATIONS;
    private List<String> players;

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
        this.players = new ArrayList<String>();
        // players contain all players
        this.players.add(this.name);
        this.players.add("null");

        System.out.println("number of stations: "+String.valueOf(NUM_STATIONS));
        // Build a Naive List Containing all paths.
        // 1- Use DFS
        
        // With this, get the distances!
        
        // Sum up all transit links of path. Print Profit!
        
        // geography - Maps Station -. (x, y)
        // tranist maps (Station 1 -> Station 2, traffic)
        
        // Init Adjacency List
        this.owner = new String[NUM_STATIONS][NUM_STATIONS];
        for (int i=0;i<NUM_STATIONS;++i){
            for (int j=0;j<NUM_STATIONS;++j){
                this.owner[i][j] = "null";
            }
        }

    }

    // helper function to print owner
    public void print_owner(){
        for (int i=0;i<NUM_STATIONS;++i){
            for (int j=0;j<NUM_STATIONS;++j){
                System.out.printf("%d to %d owned by %s\n",i,j,owner[i][j]);
            }
        }
    }

    // this now compute the marginal flow and update the values in the Revenue array

    public void update_flow(List<BidInfo> availableBids){
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
                for (int p=0; p<allPaths.size();++p){
                    for (int k=0; k<allPaths.get(0).size()-1; ++k) {
                        cost += getDistance(allPaths.get(0).get(k), allPaths.get(0).get(k+1));
                        if (Revenue.containsKey(hash(allPaths.get(0).get(k), allPaths.get(0).get(k+1)))){
                            Revenue.put(hash(allPaths.get(0).get(k), allPaths.get(0).get(k+1)), Revenue.get(hash(allPaths.get(0).get(k), allPaths.get(0).get(k+1))) + transit[i][j]/allPaths.size());
                        }
                        else{
                            Revenue.put(hash(allPaths.get(0).get(k), allPaths.get(0).get(k+1)), transit[i][j]/allPaths.size());
                        }
                    }
                    revenue[i][j] = cost * transit[i][j] * 10;
                }
            }
        }
        if (NUM_STATIONS < 80){
            getAllProfits(revenue, availableBids);
        }
    }

    private int hash(int from, int to){
        if (from > to){
            int temp = from;
            from = to;
            to = temp;
        }
        return to*NUM_STATIONS+from;
    }

    private int hash(String from, String to){
        int f = townLookup.indexOf(from);
        int t = townLookup.indexOf(to);
        
        if (f == -1){
            System.out.printf("Town %s not found",from);
        } 
        if (t ==-1){
            System.out.printf("Town %s not found",to);
        }
        return hash(f,t);
    }


    // these are helper function to determine distance between stations
    private double getDistance(Coordinates a, Coordinates b)
    {
        return Math.pow(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2), 0.5);
    }


    // these are helper function to determine distance between stations
    private double getDistance(int a, int b)
    {
        return Math.pow(Math.pow(geo.get(a).x - geo.get(b).x, 2) + Math.pow(geo.get(a).y - geo.get(b).y, 2), 0.5);
    }

    // these are helper function to determine distance between stations
    private double getDistance(String t1, String t2) {
        if (townLookup.indexOf(t1) == -1){
            System.out.printf("Town %s not found",t1);
        } 
        if (townLookup.indexOf(t2)==-1){
            System.out.printf("Town %s not found",t2);
        }
        return getDistance(townLookup.indexOf(t1), townLookup.indexOf(t2));
    }


    public Bid getBid(List<Bid> currentBids, List<BidInfo> availableBids, Bid lastRoundMaxBid) 
    {
        List<BidInfo> avail_Bids = new ArrayList<BidInfo>();
        List<Double> amounts = new ArrayList<Double>();
        BidInfo bi;
        // update the owner of paths
        if (lastRoundMaxBid != null){        
            if (lastRoundMaxBid.id1 != -1){
                bi = availableBids.get(lastRoundMaxBid.id1);
                int from = townLookup.indexOf(bi.town1);
                int to = townLookup.indexOf(bi.town2);
                if (from == -1 || to == -1){        
                    System.out.println("TOWN NOT FOUND");
                }
                else{
                    owner[from][to] = bi.owner;
                    if (!players.contains(bi.owner)){
                        players.add(bi.owner);
                    }
                }
            }
            if (lastRoundMaxBid.id2 != -1){
                bi = availableBids.get(lastRoundMaxBid.id2);
                int from = townLookup.indexOf(bi.town1);
                int to = townLookup.indexOf(bi.town2);   
                if (from == -1 || to == -1){        
                    System.out.println("TOWN NOT FOUND");
                }
                else{
                    owner[from][to] = bi.owner;
                    if (!players.contains(bi.owner)){
                        players.add(bi.owner);
                    }
                }
            }
        }

        // find out the minimum amounts we need to bid for each paths
        // -1 if already owned
        for (int i=0; i<availableBids.size();i++)
        {
            bi = availableBids.get(i);
            if (bi.owner == null) 
            {
                int from = townLookup.indexOf(bi.town1);
                int to = townLookup.indexOf(bi.town2);   
                if (owner[from][to] == "null"){
                    avail_Bids.add(bi);
                    amounts.add(bi.amount+10000.1);
                }
            }
            else
            {
                amounts.add(-1.0);
            }
        }
        if (avail_Bids.size() == 0) 
        {
            return null;
        }
        // this finds out the Revenue for each paths
        update_flow(availableBids);
        double maxb = 0;
        Coordinates s = new Coordinates(0,0);
        Coordinates d = new Coordinates(0,0);
        String maxbn = "";
        // Check if another player has made a bid for this link and up the bid by 10000
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
                if (b.amount + 10000.1 > amounts.get(b.id1))
                {
                    amounts.set(b.id1,b.amount + 10000.1);
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

                if ((dist1/dist*b.amount + 10000.1) > amounts.get(b.id1))
                {
                    amounts.set(b.id1,(dist1/dist*b.amount) + 10000.1);
                }
                if ((dist2/dist*b.amount + 10000.1) > amounts.get(b.id2))
                {
                    amounts.set(b.id2,(dist2/dist*b.amount) + 10000.1);
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
            bi = availableBids.get(i);

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
            int revenue = Revenue.get(hash(bi.town1, bi.town2));
            // System.out.printf("from %d to %d revenue: %d\n",from, to, revenue);
            // System.out.printf("price: %f budget: %f\n",winning_price, this.budget);


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

    
    // this function calculates the marginal profits by getting a link, results
    // are stored in Revenue
    private void getAllProfits(double[][] revenue, List<BidInfo> allBids) {
        // The graph now has nodes replicated for each player and a start and end node.
        int playerRev =  0;
        int newRev = 0;

        WeightedGraph g = new WeightedGraph(NUM_STATIONS*(players.size() + 2));
        for (int i=0; i<geo.size(); ++i) {
            g.setLabel(townLookup.get(i) + "#s");
            g.setLabel(townLookup.get(i) + "#e");
            for (int j=0; j<players.size(); ++j) {
                g.setLabel(townLookup.get(i) + "#" + players.get(j));
            }
        }

        for (BidInfo bi : allBids) {
            g.addEdge(bi.town1 + "#"+bi.owner, bi.town2 + "#"+bi.owner, getDistance(bi.town1, bi.town2));
            g.addDirectedEdge(bi.town1 + "#s", bi.town1 + "#" + bi.owner, 0);
            g.addDirectedEdge(bi.town2 + "#s", bi.town2 + "#" + bi.owner, 0);
            g.addDirectedEdge(bi.town1 + "#" + bi.owner, bi.town1 + "#e", 0);
            g.addDirectedEdge(bi.town2 + "#" + bi.owner, bi.town2 + "#e", 0);
        }
        
        for (int i=0; i<geo.size(); ++i) {
            for (int j=0; j<players.size(); ++j) {
                for (int k=j+1; k<players.size(); ++k) {
                    g.addEdge(townLookup.get(i) + "#" + players.get(j), townLookup.get(i) + "#" + players.get(k), 200);
                }
            }
        }

        for (int i=0; i<geo.size(); ++i) {
            int[][] prev = Dijkstra.dijkstra(g, g.getVertex(townLookup.get(i) + "#s"));
            for (int j=i+1; j<geo.size(); ++j) {
                if (transit[i][j]==0){
                    continue;
                }
                List<List<Integer>> allPaths = Dijkstra.getPaths(g, prev, g.getVertex(townLookup.get(j) + "#e"));
                // Cost per path.
                double cost = revenue[i][j] / allPaths.size();
                for (int p=0; p<allPaths.size(); ++p) {
                    double trueDist = 0.0;
                    Map<String, Double> playerDist = new HashMap<>();

                    for (String str: players) {
                        playerDist.put(str, 0.);
                    }

                    for (int k=0; k<allPaths.get(p).size()-1; ++k) {
                        String a = g.getLabel(allPaths.get(p).get(k));
                        String b = g.getLabel(allPaths.get(p).get(k+1));

                        if (a.split("#")[1].equals(b.split("#")[1])) {
                            trueDist += getDistance(a.split("#")[0], b.split("#")[0]);
                            // System.out.printf("True Dist: %f Player Dist: %f\n",trueDist,getDistance(a.split("#")[0], b.split("#")[0]));
                            playerDist.put(a.split("#")[1], playerDist.get(a.split("#")[1]) + getDistance(a.split("#")[0], b.split("#")[0]));
                            
                        }
                    }

                    for (Map.Entry<String, Double> entry : playerDist.entrySet()) {
                        if (entry.getKey().equals(this.name)){
                                playerRev += entry.getValue()/trueDist * cost;
                        }
                    }
                }
            }
        }
        for (int x=0; x<allBids.size();x++){
            BidInfo bi = allBids.get(x);
            newRev = 0;
            if (bi.owner == null){
                g.removeEdge(g.getVertex(bi.town1 + "#null"), g.getVertex(bi.town2+"#null"));
                g.addEdge(bi.town1 + "#" + this.name, bi.town2 + "#" + this.name,  getDistance(bi.town1, bi.town2));
                g.addDirectedEdge(bi.town1 + "#s", bi.town1 + "#" + this.name, 0);
                g.addDirectedEdge(bi.town2 + "#s", bi.town2 + "#" + this.name, 0);
                g.addDirectedEdge(bi.town1 + "#" + this.name, bi.town1 + "#e", 0);
                g.addDirectedEdge(bi.town2 + "#" + this.name, bi.town2 + "#e", 0);

                for (int i=0; i<geo.size(); ++i) {
                    int[][] prev = Dijkstra.dijkstra(g, g.getVertex(townLookup.get(i) + "#s"));
                    for (int j=i+1; j<geo.size(); ++j) {
                        if (transit[i][j]==0){
                            continue;
                        }
                        List<List<Integer>> allPaths = Dijkstra.getPaths(g, prev, g.getVertex(townLookup.get(j) + "#e"));
                        // Cost per path.
                        double cost = revenue[i][j] / allPaths.size();
                        

                        for (int p=0; p<allPaths.size(); ++p) {
                            double trueDist = 0.0;
                            Map<String, Double> playerDist = new HashMap<>();

                            for (String str: players) {
                                playerDist.put(str, 0.);
                            }
                            playerDist.put(this.name,0.);

                            for (int k=0; k<allPaths.get(p).size()-1; ++k) {
                                String a = g.getLabel(allPaths.get(p).get(k));
                                String b = g.getLabel(allPaths.get(p).get(k+1));

                                if (a.split("#")[1].equals(b.split("#")[1])) {
                                    trueDist += getDistance(a.split("#")[0], b.split("#")[0]);
                                    playerDist.put(a.split("#")[1], playerDist.get(a.split("#")[1]) + getDistance(a.split("#")[0], b.split("#")[0]));
                                }
                                else{
                                    trueDist += getDistance(a.split("#")[0], b.split("#")[0]);
                                }
                                //System.out.printf("%s to %s generate %s %f\n",a,b,a.split("-")[1], getDistance(a.split("-")[0], b.split("-")[0]));
                            }

                            for (Map.Entry<String, Double> entry : playerDist.entrySet()) {
                                if (entry.getKey().equals(this.name)){
                                        newRev += entry.getValue()/trueDist * cost;
                                }
                                //System.out.printf("%d to %d generate %f for %s\n", i, j, entry.getValue()/trueDist * cost, entry.getKey() );
                            }
                        }
                    }
                }
                g.removeEdge(g.getVertex(bi.town1 + "#" + this.name), g.getVertex(bi.town2 + "#" + this.name));
                g.removeEdge(g.getVertex(bi.town2 + "#" + this.name), g.getVertex(bi.town1 + "#" + this.name));
                g.removeEdge(g.getVertex(bi.town1 + "#s"), g.getVertex(bi.town1 + "#" + this.name));
                g.removeEdge(g.getVertex(bi.town2 + "#s"), g.getVertex(bi.town1 + "#" + this.name));
                g.removeEdge(g.getVertex(bi.town1 + "#" + this.name), g.getVertex(bi.town1 + "#e"));
                g.removeEdge(g.getVertex(bi.town2 + "#" + this.name), g.getVertex(bi.town2 + "#e"));
                g.addEdge(g.getVertex(bi.town1 + "#null"), g.getVertex(bi.town2+"#null"), getDistance(bi.town1,bi.town2));
                //System.out.printf(" %s to %s generate revenue %d\n",bi.town1, bi.town2, newRev-playerRev);
                Revenue.put(hash(bi.town1, bi.town2), newRev - playerRev);
            }
        } 
    }

    private static int minVertex (double[] dist, boolean[] v) {
        double x = Double.MAX_VALUE;
        int y = -1;    // graph not connected, or no unvisited vertices
        for (int i=0; i<dist.length; i++) {
            if (!v[i] && dist[i]<x) {y=i; x=dist[i];}
        }
        return y;
    }


    private double getProfit(int from, int to){
        return Revenue.get(hash(from, to));
    }


    public void updateBudget(Bid bid) 
    {
        if (bid != null) 
        {
            budget -= bid.amount;
        }
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
