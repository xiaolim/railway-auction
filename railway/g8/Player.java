package railway.g8;

import railway.sim.utils.Bid;
import railway.sim.utils.BidInfo;
import railway.sim.utils.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.HashMap;
// To access data classes.

public class Player implements railway.sim.Player 
{
    // Random seed of 42.
    private int seed = 42;
    private Random rand;

    private double budget;
    private List<BidInfo> availableBids = new ArrayList<>();
    
    // Profit calculation, 1 lira for EACH kilometer
    //private final static int MONEY_PER_MILE = 1;
    private static int NUM_STATIONS;
    private ArrayList<Integer>[] adjList; 
    private List<Coordinates> geography;
    private List<List<Integer>> station_links;
    private HashMap<Integer, Coordinates> station_to_coordinates = new HashMap<Integer, Coordinates>();
    private int [][] transit_weights;
    
    // For use with paths
    private ArrayList<Integer> pathList = new ArrayList<>(); 
   
    public Player() 
    {
        rand = new Random();
    }

    // Line 117: Simulator
    public void init(
        String name,
        double budget,
        List<Coordinates> geo,
        List<List<Integer>> infra,
        int[][] transit,
        List<String> townLookup) 
    {
    	/*
    	 * A,B
    	 * A,D
    	 * B,C
    	 * C,E
    	 * C,F
    	 * D,E
    	 * 
    	 * USE THIS TO BUILD ADJACENCY LIST FOR PATH COMPUTATIONS
    	 * Becomes--Station 0 is A, etc.
    	 * 0: 1, 3
    	 * 1: 2
    	 * 2: 4, 5
    	 * 3: 4
    	 * 4: null
    	 * 5; null
    	 */
    	for(int i = 0; i < infra.size();i++)
    	{
    		List<Integer> temp = infra.get(i);
    		System.out.println("ID: " + i);
    		for(int j = 0; j < temp.size();j++)
    		{
    			System.out.println("Connected to: " + temp.get(j));
    		}
    		System.out.println(" ");
    	}
 		for(int j = 0; j < geo.size();j++)
		{
			System.out.print("Coordinate: " + geo.get(j).x + " " + geo.get(j).y + "\n");
			station_to_coordinates.put(j, geo.get(j));
		}
 		this.NUM_STATIONS = geo.size();
 		this.geography = geo;
 		this.station_links = infra;
 		this.transit_weights = transit;
 		
 		// Build a Naive List Containing all paths.
 		// 1- Use DFS
 		
 		// With this, get the distances!
 		
 		// Sum up all transit links of path. Print Profit!
 		
    	// geography - Maps Station -. (x, y)
    	// tranist maps (Station 1 -> Station 2, traffic)
    	
    	/*
    	 * Transit Link:
    	 * A,B,100
    	 * A,C,200
    	 * A,D,50
    	 * A,E,100
    	 * A,F,150
    	 * B,C,100
    	 * B,D,200
    	 * B,E,100
    	 * B,F,100
    	 * C,D,50
    	 * C,E,150
    	 * C,F,250
    	 * D,E,200
    	 * D,F,100
    	 * E,F,50
		Becomes:
		A: 0, 100, 200, 50, 100, 150 (A, B, C, D, E, F)
		B: 0, 0, 100, 200, 100, 100  (A, B, C, D, E, F)
		C: 0, 0, 0, 50, 150, 250 (A, B, C, D, E, F)
    	 */
    	// Init Adjacency List
    	adjList = new ArrayList[NUM_STATIONS];
    	System.out.println("Number of Stations: " + NUM_STATIONS);
      	for(int i = 0; i < NUM_STATIONS;i++)
    	{
      		adjList[i] = new ArrayList<>();
      		List<Integer> row = infra.get(i);
    		for(int j = 0; j < NUM_STATIONS;j++)
    		{
    			if(row.isEmpty())
    			{
    				adjList[i].add(0);
    			}
    			else
    			{
        			if(row.contains(j))
        			{
            			adjList[i].add(j);
        			}
        			else
        			{
            			adjList[i].add(0);
        			}
    			}
    			//System.out.println("Connected to: " + temp.get(j));
    		}
    	}
      	printAllPaths(0, 5);
    	this.geography = geo;
    	//System.out.println(Arrays.deepToString(transit_link));
        this.budget = budget;
    }

    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids) 
    {
        // The random player bids only once in a round.
        // This checks whether we are in the same round.
        // Random player doesn't care about bids made by other players.
        if (availableBids.size() != 0) 
        {
            return null;
        }

        for (BidInfo bi : allBids) 
        {
            if (bi.owner == null) 
            {
                availableBids.add(bi);
            }
        }

        if (availableBids.size() == 0) 
        {
            return null;
        }

        BidInfo randomBid = availableBids.get(rand.nextInt(availableBids.size()));
        double amount = randomBid.amount;

        // Don't bid if the random bid turns out to be beyond our budget.
        if (budget - amount < 0.) 
        {
            return null;
        }

        // Check if another player has made a bid for this link.
        for (Bid b : currentBids) 
        {
            if (b.id1 == randomBid.id || b.id2 == randomBid.id) 
            {
                if (budget - b.amount - 10000 < 0.) 
                {
                    return null;
                }
                else 
                {
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

    public void updateBudget(Bid bid) 
    {
        if (bid != null) 
        {
            budget -= bid.amount;
        }
        availableBids = new ArrayList<>();
    }
    
    /*
     *  Method, get cost of a whole link:
     *  Example, if you have A -> B -> C
     *  A -> B has profit of $10,000
     *  A -> C has profit of $10,000
     *  B -> C has profot of $10,000
     *  Profit(A, B) = 10,000
     *  Profit(A, C) = 30,000
     *  
     *  Input: Two train stations.
     */
    
    // Using the Path Link and Transit Adjacency List, Get the Profit!
    public int profit_of_links()
    {
    	int profit = 0;
    	// [0, 1, 2, 5]
    	// get profit of (0, 1) + (1, 2) + (2, 5)
    	for(int i = 1; i < pathList.size()-1; i++)
    	{
    		profit += transit_weights[i][i+1];
    	}
        return profit;
    }
    
    // Prints all possible paths from source to destination
    public void printAllPaths(int s, int d)  
    { 
        boolean[] isVisited = new boolean[NUM_STATIONS]; 
          
        //add source to path[] 
        pathList.add(s); 
          
        //Call recursive utility 
        printAllPathsUtil(s, d, isVisited, pathList); 
    }
    
    // Credit to: https://www.geeksforgeeks.org/find-paths-given-source-destination/
    private void printAllPathsUtil(Integer u, Integer d, 
            boolean[] isVisited, List<Integer> localPathList)
    {
    	// Mark the current node 
    	isVisited[u] = true; 

    	// Have the class catch the paths
    	if (u.equals(d))  
    	{
    		System.out.println("Path");
    		System.out.println(localPathList);
    		System.out.println("Profit: " + profit_of_links());
    	}

    	// Recur for all the vertices 
    	// adjacent to current vertex 
    	for (Integer i : adjList[u])  
    	{ 
    		if (!isVisited[i]) 
    		{
    			// store current node  
    			// in path[] 
    			localPathList.add(i); 
    			printAllPathsUtil(i, d, isVisited, localPathList); 

    			// remove current node 
    			// in path[] 
    			localPathList.remove(i); 
    		} 
    	}

    	// Mark the current node 
    	isVisited[u] = false; 
    } 
}
