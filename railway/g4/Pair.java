package railway.g4;

import java.io.Serializable;

public class Pair implements Serializable 
{
    public int to;
    public int from;

    public Pair() {}

    public Pair(int x, int y) 
    {
        this.from = x;
        this.to = y;
    }

    public String toString()
    {
        String x = String.valueOf(from) + " -> " + String.valueOf(to);
        return x;
    }
}
