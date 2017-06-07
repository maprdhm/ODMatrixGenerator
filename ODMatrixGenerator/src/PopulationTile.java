import com.vividsolutions.jts.geom.Coordinate;

import Projections.EPSG3035Coordinate;

public class PopulationTile {
	private Coordinate centroidLatLon;
	private EPSG3035Coordinate centroidLambert2;
	private double latNorth;
	private double latSouth;
	private double lonEast;
	private double lonWest;
	private double population;
	private static double resolution;
	
	public PopulationTile(EPSG3035Coordinate centroidLambert2, double pop) {
		this.centroidLambert2 = centroidLambert2;
		this.centroidLatLon = centroidLambert2.EPSG3035ToLatLong();
		this.population = pop;
		this.latNorth = new EPSG3035Coordinate(centroidLambert2.getX_laea(),centroidLambert2.getY_laea()+ resolution/2.0).EPSG3035ToLatLong().y;
		this.latSouth = new EPSG3035Coordinate(centroidLambert2.getX_laea(),centroidLambert2.getY_laea()- resolution/2.0).EPSG3035ToLatLong().y;
		this.lonEast = new EPSG3035Coordinate(centroidLambert2.getX_laea()+ resolution/2.0, centroidLambert2.getY_laea()).EPSG3035ToLatLong().x;
		this.lonWest = new EPSG3035Coordinate(centroidLambert2.getX_laea()- resolution/2.0,centroidLambert2.getY_laea()).EPSG3035ToLatLong().x;
	}
	
	public double getPopulation() {
		return population;
	}
	
	public Coordinate getCentroid() {
		return centroidLatLon;
	}
	
	public EPSG3035Coordinate getCentroidLambert2() {
		return centroidLambert2;
	}
	
	public static double getResolution() {
		return resolution;
	}
	
	public double getLatNorth(){
		return latNorth;
	}
	
	public double getLatSouth() {
		return latSouth;
	}

	public double getLonEast() {
		return lonEast;
	}
	
	public double getLonWest() {
		return lonWest;
	}

	public static void setResolution(double resolution) {
		PopulationTile.resolution = resolution;		
	}
}
