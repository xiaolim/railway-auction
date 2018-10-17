package railway.sim.utils;

import java.util.Arrays;

public class WeightedGraph {

    private int[][]  edges;  // adjacency matrix
    private String[] labels;

    private int counter = 0;

    public WeightedGraph(int n) {
        edges  = new int[n][n];
        labels = new String[n];
    }

    public int size() {
        return labels.length;
    }

    public void setLabel(String label) {
        labels[counter++] = label;
    }

    public String getLabel(int vertex) {
        return labels[vertex];
    }

    public int getVertex(String label) {
        // Ridiculous.
        return Arrays.asList(labels).indexOf(label);
    }

    public void addEdge(int source, int target, int w) {
        addUndirectedEdge(source, target, w);
        addUndirectedEdge(target, source, w);
    }

    public void addUndirectedEdge(int source, int target, int w) {
        edges[source][target] = w;
    }

    public boolean isEdge(int source, int target) {
        return edges[source][target] > 0; 
    }
    
    public void removeEdge(int source, int target) {
        edges[source][target] = 0;
    }
    
    public int getWeight(int source, int target) {
        return edges[source][target];
    }

    public int[] neighbors(int vertex) {
        int count = 0;
        for (int i=0; i<edges[vertex].length; i++) {
            if (edges[vertex][i]>0) count++;
        }

        final int[] answer = new int[count];
        count = 0;
        for (int i=0; i<edges[vertex].length; i++) {
            if (edges[vertex][i]>0) answer[count++]=i;
        }

        return answer;
    }

    public void print() {
        for (int j=0; j<edges.length; j++) {
            System.out.print(labels[j]+": ");
            for (int i=0; i<edges[j].length; i++) {
                if (edges[j][i]>0)
                    System.out.print(labels[i]+":"+edges[j][i]+" ");
            }

            System.out.println();
        }
    }

    public static void main(String args[]) {
        final WeightedGraph t = new WeightedGraph(6);
        t.setLabel("A");
        t.setLabel("B");
        t.setLabel("C");
        t.setLabel("D");
        t.setLabel("E");
        t.setLabel("F");
        t.addEdge(0, 1, 100);
        t.addEdge(0, 3, 50);
        t.addEdge(1, 2, 100);
        t.addEdge(2, 4, 50);
        t.addEdge(2, 5, 100);
        t.addEdge(3, 4, 200);
        t.print();

        final int[] pred = Dijkstra.dijkstra(t, 0);
        for (int n=0; n<6; n++) {
            Dijkstra.printPath(t, pred, 0, n);
        }
    }
}
