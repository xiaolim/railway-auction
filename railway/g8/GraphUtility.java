package railway.g8;

import railway.sim.utils.Coordinates;

import java.util.ArrayList;
import java.util.List;

public class GraphUtility {

    private List<Coordinates> geo;
    private List<List<Integer>> infra;
    private int[][] transit;
    private List<String> townLookup;

    public double[][] adj; //adjacency matrix: direct connection between towns and their distance
    public double[][] dist; //shortest distance between all pairs
    public List[][] path; //shortest paths between all pairs

    public GraphUtility(List<Coordinates> geo, List<List<Integer>> infra, int[][] transit, List<String> townLookup) {
        this.geo = geo;
        this.infra = infra;
        this.transit = transit;
        this.townLookup = townLookup;

        initAdj();
        floydWarshall();
        //now adj, dist, path are available to uses
    }

    private void initAdj() {
        int townSize = geo.size(); //get total size of towns
        adj = new double[townSize][townSize]; //init 2D adjacency matrix
        for (int i = 0; i < townSize; i++) { //init values, if no link return INF
            for (int j = 0; j < townSize; j++) {
                if (infra.get(i).contains(j)) { //connected
                    adj[i][j] = Euclidean(geo.get(i), geo.get(j));
                } else { //set to infinity
                    adj[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    private void floydWarshall() { //run algorithm and modify adj, dist, path
        int townSize = geo.size();
        dist = new double[townSize][townSize]; //distance matrix
        int[][] next = new int[townSize][townSize]; //for path reconstruction

        //init dist and pathTrace
        for (int i = 0; i < townSize; i++) {
            for (int j = 0; j < townSize; j++) {
                dist[i][j] = adj[i][j];
                next[i][j] = -1;
            }
        }

        //dp with path
        for (int k = 0; k < townSize; k++) {
            for (int i = 0; i < townSize; i++) {
                for (int j = 0; j < townSize; j++) {
                    if (dist[i][j] > dist[i][k] + dist[k][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        path = new List[townSize][townSize];
        //get back all paths
        for (int i = 0; i < townSize; i++) {
            for (int j = 0; j < townSize; j++) {
                path[i][j] = pathTrace(i, j, next);
            }
        }
    }

    private List<Integer> pathTrace(int i, int j, int[][] next) {
        List<Integer> ret = new ArrayList<>();
        if (next[i][j] == -1) return ret;
        ret.add(i);
        while (i != j) {
            i = next[i][j];
            ret.add(i);
        }
        return ret;
    }

    public double Euclidean(Coordinates a, Coordinates b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
}
