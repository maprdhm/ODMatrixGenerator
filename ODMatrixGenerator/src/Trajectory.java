import com.vividsolutions.jts.geom.Coordinate;

/** Class representing a traject starting --> arrival with lat/long coordinates */
public class Trajectory {
	private Coordinate starting;
	private Coordinate arrival;
	
	public Trajectory(Coordinate start, Coordinate arrival) {
		this.starting = start;
		this.arrival = arrival;
	}
	
	public Coordinate getStarting() {
		return starting;
	}
	
	public Coordinate getArrival() {
		return arrival;
	}
	
	@Override
	public String toString() {
		return starting.toString()+" "+arrival.toString();
	}
}
