package railway.g7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// To access data classes.
import railway.sim.utils.*;

public class Player implements railway.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;

    private double budget;
    private String name;
    private List<Coordinates> geo;
    private List<List<Integer>> infra;
    private int[][] transit;
    private List<String> townLookup;
    private WeightedGraph graph;
    private List<RouteValue> rankedRouteValue;

    private List<BidInfo> availableBids = new ArrayList<>();

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
        this.budget = budget;
        this.geo = geo;
        this.infra = infra;
        this.transit = transit;
        this.townLookup = townLookup;
        initializeGraph();
        // List<List<Integer>> links = getMostVolumePerKm();
        // for (int i = 0; i < links.size(); i++) {
        //     System.out.println("The %s link is: ");
        //     for (int j = 0; j < links.get(i).size(); j++) {
        //         System.out.print(links.get(i).get(j) + " ");
        //     }
        // }
        List<List<Integer>> bridges = findBridges();
        System.out.println("The bridges are:");
        for (int i = 0; i < bridges.size(); i++) {
            for (int j = 0; j < bridges.get(i).size(); j++) {
                System.out.print(bridges.get(i).get(j) + " ");
            }
            System.out.println();
        }
        rankedRouteValue = new ArrayList<RouteValue>();
        gatherAllVolumePerKm();
        for (int i = 0; i < rankedRouteValue.size(); i++) {
            System.out.println("route number: " + i + ", volume: " + rankedRouteValue.get(i).getVolumePerKm() + ", distance: " + rankedRouteValue.get(i).getDistance());
        }
    }

    private void initializeGraph() {
        graph = new WeightedGraph(townLookup.size()); // 
        for (int i = 0; i < townLookup.size(); i++) { 
            graph.setLabel(townLookup.get(i)); // graph.setLabel(townLookup.get(i));
        }

        for (int i = 0; i < infra.size(); i++) { // how many links
            for (int j = 0; j < infra.get(i).size(); j++) { 
                int source = i;
                int target = infra.get(i).get(j);
                // graph.addEdge(source, target, transit[source][target]);
                double distance = calcEuclideanDistance(geo.get(source), geo.get(target));
                graph.addEdge(source, target, distance);
            }
        }
    }

    private double calcEuclideanDistance(Coordinates a, Coordinates b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    private List<List<Integer>> getLinks(int source, int target) {
        int[][] prev = Dijkstra.dijkstra(graph, source);
        return Dijkstra.getPaths(graph, prev, target);
    }

    // private List<List<Integer>> getMostVolumePerKm() {
    //     double max = 0;
    //     List<List<Integer>> maxLinks = new ArrayList<List<Integer>>();
    //     for (int i = 0; i < transit.length; i++) {
    //         for (int j = i + 1; j < transit[i].length; j++) {
    //             List<List<Integer>> links = getLinks(i, j);
    //             System.out.println("transit: s=" + i + ", t=" + j);
    //             for (int m = 0; m < links.size(); m++) {
    //                 for (int n = 0; n < links.get(m).size(); n++) {
    //                     System.out.print(links.get(m).get(n) + " ");
    //                 }
    //             }
    //             int distance = 0;
    //             for (int x = 0; x < links.get(0).size() - 1; x++) {
    //                 distance += graph.getWeight(links.get(0).get(x), links.get(0).get(x + 1));
    //             }

    //             double volumePerKm = (double)transit[i][j] / distance;
    //             if (volumePerKm > max) {
    //                 max = volumePerKm;
    //                 maxLinks = links;
    //             }
    //         }
    //     }

    //     System.out.println("most volume per km: " + max);
    //     return maxLinks;
    // }

    private void getIdealVolumePerKm() {
        // look into the rest of thte graph
        // take into the account what the enemy group has 
        // stop only if... 
    }

    private void chessCornerEnemy() {
        
    }

    private RouteValue getRouteValue(int source, int target) {
        List<List<Integer>> links = getLinks(source, target);
        int distance = 0;
        for (int i = 0; i < links.get(0).size() - 1; i++) {
            distance += graph.getWeight(links.get(0).get(i), links.get(0).get(i + 1));
        }
        double volumePerKm = (double) transit[source][target] / distance;
        return new RouteValue(links, volumePerKm, distance); // 
    }

    private void gatherAllVolumePerKm() {
        // double max = 0;
        List<List<Integer>> maxLinks = new ArrayList<List<Integer>>(); 
        for (int i = 0; i < transit.length; i++) {
            for (int j = i + 1; j < transit[i].length; j++) {
                rankedRouteValue.add(getRouteValue(i, j)); // get transit/distance
                // List<List<Integer>> links = getLinks(i, j);
                // System.out.println("transit: s=" + i + ", t=" + j);
                // for (int m = 0; m < links.size(); m++) {
                //     for (int n = 0; n < links.get(m).size(); n++) {
                //         System.out.print(links.get(m).get(n) + " ");
                //     }
                // }
                // int distance = 0;
                // for (int x = 0; x < links.get(0).size() - 1; x++) {
                //     distance += graph.getWeight(links.get(0).get(x), links.get(0).get(x + 1));
                // }

                // double volumePerKm = (double)transit[i][j] / distance;
                // RouteValue rv = new RouteValue(links, volume, distance);
                // rankedRouteValue.add(rv);
                // if (volumePerKm > max) {
                //     max = volumePerKm;
                //     maxLinks = links;
                // }
            }
        }
        Collections.sort(rankedRouteValue, Collections.reverseOrder());
    }

    private List<List<Integer>> findBridges() {
        List<List<Integer>> bridges = new ArrayList<List<Integer>>();
        for (int i = 0; i < infra.size(); i++) {
            for (int j = 0; j < infra.get(i).size(); j++) {
                int source = i;
                int target = infra.get(i).get(j);
                double weight = graph.getWeight(source, target);
                graph.removeEdge(source, target);
                boolean bridgeFound = false;
                // System.out.println("source: " + source + ", target: " + target);
                for (int s = 0; s < townLookup.size(); s++) {
                    int[][] prev = Dijkstra.dijkstra(graph, s);
                    for (int t = s + 1; t < prev.length; t++) {
                        if (prev[t][0] == 0) {
                            bridgeFound = true;
                            // System.out.println("s: " + s + ", t: " + t);
                            break;
                        }
                    }

                    if (bridgeFound) {
                        break;
                    }
                }

                if (bridgeFound) {
                    List<Integer> bridge = new ArrayList<Integer>();
                    bridge.add(source);
                    bridge.add(target);
                    bridges.add(bridge);
                }

                graph.addEdge(source, target, weight);
            }
        }

        return bridges;
    }

    // return true if link is owned by someone else
    public boolean checkOwnershipByTownID(int id1, int id2){
        for (BidInfo bi : allBids) {
            if (((bi.town1==id1 && bi.town2==id2)||(bi.town1==id2 && bi.town2==id1))&&bi.owner != null && bi.owner != this.name) {
                return true;
            }
        }
        return false;
    }

    public Bid getBid(List<Bid> currentBids, List<BidInfo> allBids) {
        // The random player bids only once in a round.
        // This checks whether we are in the same round.
        // Random player doesn't care about bids made by other players.
        if (availableBids.size() != 0) {
            return null;
        }

        for (BidInfo bi : allBids) {
            if (bi.owner == null) {
                availableBids.add(bi);
            }
        }

        if (availableBids.size() == 0) {
            return null;
        }

        RouteValue routeToBid;
        for (int i=0; i< rankedRouteValue.size();i++){
            routeToBid = rankedRouteValue.get(i);
            if (!routeToBid.hasOtherOwner){ // if the route does not any links owned by another group
                break;
            }
        }
        // get the first two bids
        LinkValue link1 = routeToBid.linkValues.get(0); 
        LinkValue link2 = routeToBid.linkValues.get(1); 

        double amout;
        amount = link1.distance * transit[link1.town1][link1.town2];
        if (link2 != null) {
            amount += link2.distance * transit[link2.town1][link2.town2];
        }

        // taking into account the entire route 
        amount += 0.2 * routeToBid.volPerKm * routeToBid.distance // the entire distance? 

        // Don't bid if the random bid turns out to be beyond our budget.
        if (budget - amount < 0.) {
            return null;
        }

        // Check if another player has made a bid for this link.
        for (Bid b : currentBids) {
            if (b.id1 == randomBid.id || b.id2 == randomBid.id) {
                if (budget - b.amount - 10 < 0.) {
                    return null;
                }
                else {
                    amount = b.amount + 10;
                }

                break;
            }
        }

        Bid bid = new Bid();
        bid.amount = amount;
        bid.id1 = randomBid.id;
        return bid;
    }

    public void updateBudget(Bid bid) {
        if (bid != null) {
            budget -= bid.amount;
        }

        availableBids = new ArrayList<>();
    }

    private class LinkValue implements Comparable<LinkValue>{
        int town1;
        int town2;
        double distance;

        public LinkValue (int id1, int id2){
            town1 = id1;
            town2 = id2;
            distance = graph.getWeight(id1,id2); // weight = distance 
        }

        @Override
        public int compareTo(LinkValue lv) {
            return (int) Math.signum(distance - lv.distance);
        }
    }

    private class RouteValue implements Comparable<RouteValue>{
        List<List<Integer>> routes;
        double volPerKm;
        double distance;
        List<LinkValue> linkValues= new ArrayList<LinkValue>();
        public boolean hasOtherOwner;

        public RouteValue (List<List<Integer>> r, double v, double d) {
            routes = copyListofList(r);
            volPerKm = v;
            distance = d;
            hasOtherOwner = false;
            for (int i=0; i < routes.size()-1;i++){
                linkValues.add(new LinkValue(routes[i],routes[i+1]));
            }
            Collections.sort(linkValues, Collections.reverseOrder());
        }

        private List<List<Integer>> copyListofList(List<List<Integer>> list) {
            List<List<Integer>> results = new ArrayList<List<Integer>>();
            for (int i = 0; i < list.size(); i++) {
                List<Integer> result = new ArrayList<Integer>();
                for (int j = 0; j < list.get(i).size(); j++) {
                    result.add(list.get(i).get(j));
                }
                results.add(result);
            }
            return results;
        }

        // return true if link is owned by someone else
        public boolean checkOwnership(){
            for (int i=0; i < routes.size()-1;i++){
                String id1 = townLookup.get(routes[i]);
                String id2 = townLookup.get(routes[i+1]);
                hasOtherOwner = checkOwnershipByTownID(id1, id2);
                if (hasOtherOwner==true){
                    return true;
                }
            }
            return false;
        }

        public List<List<Integer>> getRoutes() {
            return copyListofList(routes);
        }

        public double getVolumePerKm() {
            return volPerKm;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(RouteValue rv) {
            if (volPerKm != rv.volPerKm) {
                return (int) Math.signum(volPerKm - rv.volPerKm);
            }
            else {
                return (int) Math.signum(distance - rv.distance);
            }
        }
    }
}
