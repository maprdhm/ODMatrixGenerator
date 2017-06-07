package Projections;

public class GISComputation {

	public static double GPS2Meter(double latitudeInDegA, double longitudeInDegA, double latitudeInDegB, double longitudeInDegB){
		//haversine formula https://en.wikipedia.org/wiki/Haversine_formula
		double earthRadius = 6371000;
		double lat1 = Math.toRadians(latitudeInDegA);
		double lon1 = Math.toRadians(longitudeInDegA);
		double lat2 = Math.toRadians(latitudeInDegB);
		double lon2 = Math.toRadians(longitudeInDegB);
		double a = Math.pow(Math.sin((lat1 - lat2)/2),2) + Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin((lon1-lon2)/2),2);
		//double a = Math.pow(Math.sin((Math.toRadians(45.7593642) - Math.toRadians(45.7592012))/2),2) + Math.cos(Math.toRadians(45.7593642))*Math.cos(Math.toRadians(45.7592012))*Math.pow(Math.sin((Math.toRadians(4.8446732)-Math.toRadians(4.8424835))/2),2);
		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = earthRadius*c;
		return d;
	}
	
}
