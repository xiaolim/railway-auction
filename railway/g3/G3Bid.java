package railway.g3;

// To access data classes.
import railway.sim.utils.*;

import java.util.Arrays;

public class G3Bid extends Bid implements Comparable<Bid> {
	// id of first town
	public int town_id1;

	// id of second town
	public int town_id2;

	// id of third town -- if applicable
	public int town_id3;

	// minimum bid that could ever win the link
	public double min_bid;

	public G3Bid() {
		town_id1 = -1;
		town_id2 = -1;
		town_id3 = -1;
	}

	@Override
	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}
		G3Bid otherBid = (G3Bid) o;
		if ((otherBid.town_id3 < -1 && this.town_id3 > -1) || (otherBid.town_id3 > -1 && this.town_id3 < -1)) {
			return false;
		}
		int[] otherIDs = {otherBid.town_id1, otherBid.town_id2, otherBid.town_id3};
		int[] thisIDs = {this.town_id1, this.town_id2, this.town_id3};
		Arrays.sort(otherIDs);
		Arrays.sort(thisIDs);
		if (Arrays.equals(otherIDs, thisIDs)) {
			return true;
		}
		return false;

	}

	@Override
	public int compareTo(Bid other) {
		return (int)Math.signum(this.amount - other.amount);
	}
}