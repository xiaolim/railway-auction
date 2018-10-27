package railway.g8;

import railway.sim.utils.Coordinates;

import java.util.List;

public class GraphUtility {

    private List<Coordinates> geo;
    private List<List<Integer>> infra;
    private int[][] transit;
    private List<String> townLookup;

    public double[][] adj; //adjacency matrix: direct connection between towns and their distance
    public double[][] dist; //shortest distance between all pairs
    public List<Integer>[][] path; //shortest paths between all pairs

    public GraphUtility(List<Coordinates> geo, List<List<Integer>> infra, int[][] transit, List<String> townLookup) {
        this.geo = geo;
        this.infra = infra;
        this.transit = transit;
        this.townLookup = townLookup;
    }

    private void initAdj() {
        int townSize = geo.size(); //get total size of towns
        adj = new double[townSize][townSize]; //init 2D adjacency matrix
        for (int i = 0; i < townSize; i++) { //init values, if no link return INF
            for (int j = 0; i < townSize; j++) {
                if (infra.get(i).contains(j)) { //connected
                    adj[i][j] = Euclidean(geo.get(i), geo.get(j));
                } else { //set to infinity
                    adj[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    private void algo() { //run algorithm and modify adj, dist, path

    }

    public double Euclidean(Coordinates a, Coordinates b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
}
