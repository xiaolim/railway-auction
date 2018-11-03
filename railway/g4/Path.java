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

public class Path implements Comparable<Path>
{
	public final static int PENALTY = 200;
	public List<Integer> path = new ArrayList<Integer>(); // One Path
	public double distance = 0;
	
	// Store data about paths
	public Path()
	{
		
	}
	
	// Only use for two adjacent
	private static double getDistance(int from_station, int to_station)
	{
		return Math.pow(Math.pow(geo.get(a).x - geo.get(b).x, 2) + Math.pow(geo.get(a).y - geo.get(b).y, 2), 0.5);
	}
	
	// Sum distance of a whole path...
    public double distance_of_links(List<Integer> list_of_stations)
    {
    	double distance = 0;
        // [0, 1, 2, 5]
        // get distance of (0, 1) + (1, 2) + (2, 5)
        for(int i = 0; i < list_of_stations.size(); i++)
        {
        	// Check if there is a jump in ownder
        	if()
        	{
        		distance += PENALTY;
        	}
            distance += getDistance(i, i+1);
        }
        return profit;
    }
    
    public void update()
    {
    	// call distance_of_links...
    }
    
    // Easy to get minimum path
	public int compareTo(Path other)
	{
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than 
        // other and 0 if they are supposed to be equal
		if(this.distance < other.distance)
		{
			return -1;
		}
		else if(this.distance == other.distance)
		{
			return 0;
		}
		else
		{
			return 1;
		}
    }
}
