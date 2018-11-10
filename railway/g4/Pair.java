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
}
