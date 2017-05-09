
public class UTMCoordinate {
	private double easting;
	private double northing;
	private int zone;
	private char letter;
	
	public UTMCoordinate(double utmEast, double utmNorth, int zone, char letter) {
		this.easting = utmEast;
		this.northing = utmNorth;
		this.zone = zone;
		this.letter = letter;
	}
	
	public double getEasting() {
		return easting;
	}
	
	public double getNorthing() {
		return northing;
	}
	
	public int getZone() {
		return zone;
	}
	
	public char getLetter() {
		return letter;
	}
	
	public UTMCoordinate(double lat,double lon)
	    {
	        zone= (int) Math.floor(lon/6+31);
	        if (lat<-72) 
	            letter='C';
	        else if (lat<-64) 
	            letter='D';
	        else if (lat<-56)
	            letter='E';
	        else if (lat<-48)
	            letter='F';
	        else if (lat<-40)
	            letter='G';
	        else if (lat<-32)
	            letter='H';
	        else if (lat<-24)
	            letter='J';
	        else if (lat<-16)
	            letter='K';
	        else if (lat<-8) 
	            letter='L';
	        else if (lat<0)
	            letter='M';
	        else if (lat<8)  
	            letter='N';
	        else if (lat<16) 
	            letter='P';
	        else if (lat<24) 
	            letter='Q';
	        else if (lat<32) 
	            letter='R';
	        else if (lat<40) 
	            letter='S';
	        else if (lat<48) 
	            letter='T';
	        else if (lat<56) 
	            letter='U';
	        else if (lat<64) 
	            letter='V';
	        else if (lat<72) 
	            letter='W';
	        else
	            letter='X';
	        easting=0.5*Math.log((1+Math.cos(lat*Math.PI/180)*Math.sin(lon*Math.PI/180-(6*zone-183)*Math.PI/180))/(1-Math.cos(lat*Math.PI/180)*Math.sin(lon*Math.PI/180-(6*zone-183)*Math.PI/180)))*0.9996*6399593.62/Math.pow((1+Math.pow(0.0820944379, 2)*Math.pow(Math.cos(lat*Math.PI/180), 2)), 0.5)*(1+ Math.pow(0.0820944379,2)/2*Math.pow((0.5*Math.log((1+Math.cos(lat*Math.PI/180)*Math.sin(lon*Math.PI/180-(6*zone-183)*Math.PI/180))/(1-Math.cos(lat*Math.PI/180)*Math.sin(lon*Math.PI/180-(6*zone-183)*Math.PI/180)))),2)*Math.pow(Math.cos(lat*Math.PI/180),2)/3)+500000;
	        easting=Math.round(easting*100)*0.01;
	        northing = (Math.atan(Math.tan(lat*Math.PI/180)/Math.cos((lon*Math.PI/180-(6*zone -183)*Math.PI/180)))-lat*Math.PI/180)*0.9996*6399593.625/Math.sqrt(1+0.006739496742*Math.pow(Math.cos(lat*Math.PI/180),2))*(1+0.006739496742/2*Math.pow(0.5*Math.log((1+Math.cos(lat*Math.PI/180)*Math.sin((lon*Math.PI/180-(6*zone -183)*Math.PI/180)))/(1-Math.cos(lat*Math.PI/180)*Math.sin((lon*Math.PI/180-(6*zone -183)*Math.PI/180)))),2)*Math.pow(Math.cos(lat*Math.PI/180),2))+0.9996*6399593.625*(lat*Math.PI/180-0.005054622556*(lat*Math.PI/180+Math.sin(2*lat*Math.PI/180)/2)+4.258201531e-05*(3*(lat*Math.PI/180+Math.sin(2*lat*Math.PI/180)/2)+Math.sin(2*lat*Math.PI/180)*Math.pow(Math.cos(lat*Math.PI/180),2))/4-1.674057895e-07*(5*(3*(lat*Math.PI/180+Math.sin(2*lat*Math.PI/180)/2)+Math.sin(2*lat*Math.PI/180)*Math.pow(Math.cos(lat*Math.PI/180),2))/4+Math.sin(2*lat*Math.PI/180)*Math.pow(Math.cos(lat*Math.PI/180),2)*Math.pow(Math.cos(lat*Math.PI/180),2))/3);
	        if (letter<'M')
	            northing = northing + 10000000;
	        northing=Math.round(northing*100)*0.01;
	    }

}
