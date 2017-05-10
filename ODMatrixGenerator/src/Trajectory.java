
public class Trajectory {
	private LatLongCoordinate starting;
	private LatLongCoordinate arrival;
	
	public Trajectory(LatLongCoordinate start, LatLongCoordinate arrival) {
		this.starting = start;
		this.arrival = arrival;
	}
	
	public LatLongCoordinate getStarting() {
		return starting;
	}
	
	public LatLongCoordinate getArrival() {
		return arrival;
	}
	
	@Override
	public String toString() {
		return starting.toString()+" "+arrival.toString();
	}
}
