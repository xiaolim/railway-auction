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

public class Player implements railway.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;

    private double budget;

    private List<BidInfo> availableBids = new ArrayList<>();
    
    // use maps to store each player's budget and arrays for links
    private HashMap<String, Double> allPlayerBudget = new HashMap<>();

    private HashMap<String, LinkedList<String>> allPlayerLinks = new HashMap<>();
    private List<Coordinates> geo;
    private List<String> townLookup;
    private int [][] revenue;
    private List<List<Integer>> infra;
    private int [][] transit;
    private String name;
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
        this.name = name;
        this.townLookup = townLookup;
        this.geo = geo;
        this.budget = budget;
        this.infra = infra;
        this.transit = transit;
        update_rev();
        System.out.println(budget);
    }

    // This function update the corresponding revenue on each edge of the map
    private void update_rev(){
        int n = geo.size();

        // Create the graph.
        WeightedGraph g = new WeightedGraph(n);
         // Create the graph.
        
        for (int i=0; i<n; ++i) {
            g.setLabel(townLookup.get(i));
        }
        this.revenue = new int[n][n];

        for (int i=0; i<infra.size(); ++i) {
            for (int j=0; j<infra.get(i).size(); ++j) {
                g.addEdge(i, infra.get(i).get(j), getDistance(i, infra.get(i).get(j)));
            }
        }


        for (int i=0; i<n; ++i) {
            int[][] prev = Dijkstra.dijkstra(g, i);
            for (int j=i+1; j<n; ++j) {
                List<List<Integer>> allPaths = Dijkstra.getPaths(g, prev, j);

                double cost = 0;
                for (int k=0; k<allPaths.get(0).size()-1; ++k) {
                    cost = getDistance(allPaths.get(0).get(k), allPaths.get(0).get(k+1));
                    revenue[allPaths.get(0).get(k)][allPaths.get(0).get(k+1)]+= cost * transit[i][j] * 10;
                    revenue[allPaths.get(0).get(k+1)][allPaths.get(0).get(k)]+= cost * transit[i][j] * 10;
                }
            }
        }
    }

    
    private double getDistance(Coordinates a, Coordinates b){
        return Math.pow(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2), 0.5);
    }


    private double getDistance(int a, int b){
        return Math.pow(Math.pow(geo.get(a).x - geo.get(b).x, 2) + Math.pow(geo.get(a).y - geo.get(b).y, 2), 0.5);
    }


    public Bid getBid(List<Bid> currentBids, List<BidInfo> availableBids) {
        // The random player bids only once in a round.
        // This checks whether we are in the same round.
        // Random player doesn't care about bids made by other players.
        
        for (BidInfo b:availableBids){
            System.out.println(String.valueOf(b.id) + " " + b.town1 + " "+b.town2 + " "+String.valueOf(b.amount) + " "+b.owner);

        }

        for (Bid b: currentBids){
            System.out.println(String.valueOf(b.id1) + " " + String.valueOf(b.id2) + " "+String.valueOf(b.amount) + " "+b.bidder);            
        }
        
        System.out.println("0");
        
        LinkedList<String> links = new LinkedList<>();

        // *************************************************************************
        // Uses availableBids to keep track of each player's budget and links bought
        // *************************************************************************
        for (BidInfo b : availableBids) {
            if (b.owner != null) {
                String link = b.town1 + "-" + b.town2;

                // check if link is in allPlayerLinks. If not, put it in and update budget
                if (allPlayerLinks.get(b.owner) == null) {
                    Double oldBudget = allPlayerBudget.get(b.owner);
                    Double newBudget = oldBudget - b.amount;
                    allPlayerBudget.put(b.owner, newBudget);
                    //put link in links
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

        // System.out.println("taken links: ");
        // for (Map.Entry<String, LinkedList<String>> entry : allPlayerLinks.entrySet() ) {
        //     String key = entry.getKey();
        //     LinkedList<String> value = entry.getValue();
        //     System.out.print(key + ": " + value);
        //     System.out.println();
            
        // }  

        // System.out.println("group budget: ");
        // for (Map.Entry<String, Double> entry : allPlayerBudget.entrySet() ) {
        //     String key = entry.getKey();
        //     Double value = entry.getValue();
        //     System.out.print(key + ": ");
        //     System.out.println(value);
        // }        
        
        List<BidInfo> avail_Bids = new ArrayList<BidInfo>();
        List<Integer> amounts = new ArrayList<Integer>();

        for (int i=0; i<availableBids.size();i++){
      
            BidInfo bi = availableBids.get(i);
            if (bi.owner == null) {
                avail_Bids.add(bi);
                amounts.add((int)(bi.amount+10000));
            }
            else{
                amounts.add(-1);
            }
        }

        if (avail_Bids.size() == 0) {
            return null;
        }
      
        double maxb = 0;
        Coordinates s = new Coordinates(0,0);
        Coordinates d = new Coordinates(0,0);
        String maxbn = "";
        // Check if another player has made a bid for this link.
        for (int i=0; i<currentBids.size();i++){
            Bid b = currentBids.get(i);

            if (b.id2 == -1){

                if (townLookup.indexOf(availableBids.get(b.id1).town1) == -1 || townLookup.indexOf(availableBids.get(b.id1).town2) == -1){
                   System.out.println("Town not found");
                   continue;
                }
                s = geo.get(townLookup.indexOf(availableBids.get(b.id1).town1));
                d = geo.get(townLookup.indexOf(availableBids.get(b.id1).town2));

                double dist = getDistance(s,d);

                if (b.amount/dist > maxb){
                   maxb = b.amount / dist;
                   maxbn = b.bidder;
                }
                if ((int)b.amount + 10000 > amounts.get(b.id1)){
                    amounts.set(b.id1,(int)b.amount + 10000);
                }
            }
            else{

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
                if (b.amount/dist > maxb){
                   maxb = b.amount / dist;
                   maxbn = b.bidder;
                }

                if ((int)(dist1/dist*b.amount + 10000) > amounts.get(b.id1)){
                    amounts.set(b.id1,(int)(dist1/dist*b.amount) + 10000);
                }
                if ((int)(dist2/dist*b.amount + 10000) > amounts.get(b.id2)){
                    amounts.set(b.id2,(int)(dist2/dist*b.amount) + 10000);
                }
            }
        }

        if (maxbn.equals(name)){
            return null;
        }

        for (int i=0; i<revenue.length; i++){
            for (int j=0; j<revenue[i].length; j++){
                System.out.println(String.valueOf(i)+" to "+String.valueOf(j) + " : "+String.valueOf(revenue[i][j]));
            }
        }
        int profit = 0;
        Bid b = new Bid();

        for (int i=0; i<availableBids.size();i++){
            
            BidInfo bi = availableBids.get(i);

            if (townLookup.indexOf(bi.town1) == -1 || townLookup.indexOf(bi.town2) == -1){
               System.out.println("Town not found");
               continue;
            }
            //System.out.println(revenue[townLookup.indexOf(bi.town1)+1][townLookup.indexOf(bi.town2)+1]);
            if (amounts.get(i)==-1){
                continue;
            }
            int win_bid = (int)(maxb*getDistance(geo.get(townLookup.indexOf(bi.town1)),geo.get(townLookup.indexOf(bi.town2))));
            int winning_price = Math.max(amounts.get(i),win_bid);
            if (winning_price < revenue[townLookup.indexOf(bi.town1)][townLookup.indexOf(bi.town2)]){
                if (profit < revenue[townLookup.indexOf(bi.town1)][townLookup.indexOf(bi.town2)] - winning_price){
                    if (winning_price > budget){
                        continue;
                    }
                    b.id1 = i;
                    b.amount = winning_price;
                    profit = revenue[townLookup.indexOf(bi.town1)][townLookup.indexOf(bi.town2)] - winning_price;
                    System.out.println(bi.town1+ " to "+bi.town2 + " : "+String.valueOf(profit));
                }
                else{
                    profit = revenue[townLookup.indexOf(bi.town1)][townLookup.indexOf(bi.town2)] - winning_price;
                    System.out.println(bi.town1+ " to "+bi.town2 + " : "+String.valueOf(profit));
                }
            }            
        }
        if (b.id1 == -1){
            return null;
        }
        return b;
    }

    public void updateBudget(Bid bid) {
        if (bid != null) {
            budget -= bid.amount;
        }

        availableBids = new ArrayList<>();
    }
}
