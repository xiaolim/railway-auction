package railway.g3;

// To access data classes.
import railway.sim.utils.*;

public class G3Bid extends Bid {
	// id of first town
	public int town_id1;

	// id of second town
	public int town_id2;

	// id of third town -- if applicable
	public int town_id3;

	// minimum bid that could ever win the link
	public double min_bid;
<<<<<<< HEAD

	public G3Bid() {
		town_id1 = -1;
		town_id2 = -1;
		town_id3 = -1;
	}
=======
>>>>>>> 51928a424bd95fbd5ddd6bbed7d17c0408e8b16b
}