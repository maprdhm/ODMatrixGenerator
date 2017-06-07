package Projections;
import com.vividsolutions.jts.geom.Coordinate;

//https://georezo.net/forum/viewtopic.php?pid=285801
public class EPSG3035Coordinate {
	private double x_laea;
	private double y_laea;
	
	public EPSG3035Coordinate(double x, double y) {
		x_laea = x;
		y_laea = y;
	}
	
	public double getX_laea() {
		return x_laea;
	}
	
	public double getY_laea() {
		return y_laea;
	}
	
	
	public Coordinate EPSG3035ToLatLong(){
		double a = 6378137.0;
		double e = 0.081819191043;
		double lambda0 = 0.174532925199;
		double phi1 = 0.907571211037;
		double x0 = 4321000;
		double y0 = 3210000;
		double epsilon = Math.pow(10,-11);
		
		double x = x_laea-x0;
		double y = y_laea-y0;
		
		double qp = (1-Math.pow(e,2))*(1/(1-Math.pow(e,2)) - 1/(2*e) * Math.log((1-e)/(1+e)));
		
		double q1 = (1-Math.pow(e,2))*(Math.sin(phi1)/(1-Math.pow(e,2)*Math.pow(Math.sin(phi1),2)) - (1/(2*e)) * Math.log((1-e*Math.sin(phi1))/(1+e*Math.sin(phi1))));
		double beta1 = Math.asin(q1/qp);
		double m1 = Math.cos(phi1)/Math.sqrt(1-Math.pow(e,2) * Math.pow(Math.sin(phi1),2));
		double Rq = a*Math.sqrt(qp/2);
		double D = (a*m1)/(Rq*Math.cos(beta1));
		double rho = Math.sqrt(Math.pow((x/D),2) + Math.pow((D*y),2));
		double ce = 2*Math.asin(rho/(2*Rq));

		double q = qp*(Math.cos(ce)*Math.sin(beta1) + D*y*Math.sin(ce)*Math.cos(beta1)/rho);

		double lambda = lambda0 + Math.atan((x*Math.sin(ce))/((D*rho*Math.cos(beta1)*Math.cos(ce)) - Math.pow(D,2)*y*Math.sin(beta1)*Math.sin(ce)));
		double phi0 = Math.asin(q/2);

		double phi_iplus1 = 100;
		double phi_i = phi0;
		double  ecart = 100;
		while(ecart>epsilon)
		{
			phi_iplus1 = phi_i + (Math.pow((1-Math.pow(e,2) * Math.pow(Math.sin(phi_i),2)),2) / (2*Math.cos(phi_i)))*( q/(1-Math.pow(e,2))-Math.sin(phi_i)/(1-Math.pow(e,2) * Math.pow(Math.sin(phi_i),2))+(1/(2*e))*Math.log((1-e*Math.sin(phi_i)) / (1+e*Math.sin(phi_i))));
			ecart = Math.abs(phi_iplus1-phi_i);
		    phi_i = phi_iplus1;
		}
		double phi = phi_i;

		Coordinate coordinate = new Coordinate(lambda*(180/Math.PI),phi*(180/Math.PI));
		return coordinate;
	}

}
