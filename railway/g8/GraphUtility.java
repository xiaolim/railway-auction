package railway.g8;

import railway.sim.utils.Coordinates;

import java.util.List;

public class GraphUtility {

    public double[][] adj; //adjacency matrix: direct connection between towns and their distance
    public double[][] dist; //shortest distance between all pairs
    public List<Integer>[][] path; //shortest paths between all pairs

    public GraphUtility( List<Coordinates> geo, List<List<Integer>> infra, int[][] transit){

    }
    private void algo(){ //run algorithm and modify adj, dist, path

    }
}
