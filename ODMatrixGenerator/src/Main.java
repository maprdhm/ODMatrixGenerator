import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.tc33.jheatchart.HeatChart;

import com.vividsolutions.jts.geom.Coordinate;

import Projections.EPSG3035Coordinate;
import Projections.UTMCoordinate;
import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5LibraryException;
import net.iryndin.jdbf.core.DbfRecord;
import net.iryndin.jdbf.reader.DbfReader;

public class Main {
	private static int NB_TRAJECTORIES = 100000;
    private static final String HADOOP_FILENAME = "input\\Lyon.h5";
    private static final String INSEE_POPULATION_FILENAME = "input\\car_m.dbf";
    private static final double RESOLUTION = 200.0; // INSEE resolution en metre
    private static final String BBOX_GROUPNAME = "bbox";
    private static final String ACTIVITY_DENSITY_GROUPNAME = "kde_activity_100";
    private static final String RESIDENTIAL_DENSITY_GROUPNAME = "kde_residential_100";
    private static final String GRID_GROUPNAME = "xy_grid_100";
    private static final String BBOX_DATASETNAME = "values";
    private static final String DATASETNAME = "block0_values";
    private static String outputFileJson="output\\trajectories_";
    private static String cityName;
    private static String outputDirHeatMap="output\\HeatMaps\\";

    private static double [][] activityDensityMatrix;
    private static double [][] residentialDensityMatrix;
    private static double [][][] coordUTMMatrix;
    private static double [] bboxMatrix;
    
    private static ArrayList<Coordinate> coordList;
    private static ArrayList<Double> activityDensityList;
    private static ArrayList<Double> residentialDensityList;
    private static ArrayList<Double> activityDensityListCumul;
    private static ArrayList<Double> residentialDensityListCumul;
    
    private static ArrayList<Trajectory> trajectories;
    private static int[] randomResidences, randomActivities;
    
    
    public static void main(String[] args) 
    {
    	cityName = HADOOP_FILENAME.substring(HADOOP_FILENAME.lastIndexOf("\\")+1, HADOOP_FILENAME.lastIndexOf("."));

    	//Load bbox data, grid data and activity density from h5 file
    	int file_id = -1;
        try {
            file_id = H5.H5Fopen(HADOOP_FILENAME, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
            loadMatrix(file_id, BBOX_GROUPNAME, BBOX_DATASETNAME);
            loadMatrix(file_id, GRID_GROUPNAME, DATASETNAME);
            loadMatrix(file_id, ACTIVITY_DENSITY_GROUPNAME, DATASETNAME);
            H5.H5Fclose(file_id);
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            
        
        //Load population in bbox
    	long beginTime = System.currentTimeMillis();
    	List<PopulationTile> populations = loadPopulation();
		System.out.println("Time to load population : " + (System.currentTimeMillis() - beginTime) + "ms.");
    		
		if(populations.isEmpty()){
			try {
				file_id = H5.H5Fopen(HADOOP_FILENAME, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
		        loadMatrix(file_id, RESIDENTIAL_DENSITY_GROUPNAME, DATASETNAME);
		        H5.H5Fclose(file_id);
			}
	        catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
		beginTime = System.currentTimeMillis();
		matrixToLists();
		listsToCumulativeLists();
		System.out.println("Time to convert in list : " + (System.currentTimeMillis() - beginTime) + "ms.");

		beginTime = System.currentTimeMillis();
		generateTrajectories(populations);
		System.out.println("Time to generate trajectories : " + (System.currentTimeMillis() - beginTime) + "ms.");
    	
    	drawHeatMaps();
    	outputFileJson+= cityName +".json";
    	JSONFileWriter.writeTrajectories(trajectories, outputFileJson);
    
    }
    
    
    
  //Generate random trajectories
  	private static void generateTrajectories(List<PopulationTile> populationTiles) {  	
  		trajectories = new ArrayList<>();
  		randomResidences = new int[coordList.size()];
  		randomActivities = new int[coordList.size()];
  		
  		if(!populationTiles.isEmpty()){
  			NB_TRAJECTORIES = 0;
  			for(PopulationTile p: populationTiles)
  				NB_TRAJECTORIES+=Math.round(p.getPopulation());
  		}
		System.out.println(NB_TRAJECTORIES);

		List <Coordinate> listResidentialCoordinates = loadResidentialCoordinates(populationTiles);
  		

  		 //Random rand = new Random(987654321);
		Random rand = new Random(System.currentTimeMillis());
  		 for(int i=0; i < NB_TRAJECTORIES; i++){
  			 Coordinate residence = null;
  			 Coordinate activity = null;
  			 
  			 int activityIterator = 0;
  			 double randActiv = rand.nextDouble();
  			 
  			 while(activityIterator< activityDensityListCumul.size() &&
  				randActiv>activityDensityListCumul.get(activityIterator)) {
  				 if(randActiv>activityDensityListCumul.get(activityIterator))
  						 activityIterator++;
  			 }
  			 if(activityIterator < coordList.size()){
  				 activity =  new Coordinate(coordList.get(activityIterator));
  				 randomActivities[activityIterator]+=1;
  			 }
  			 
  			 if(listResidentialCoordinates.isEmpty() && !residentialDensityListCumul.isEmpty()){
  				 int residentialIterator = 0;
  				 double randResid = rand.nextDouble();

  				 while(residentialIterator < residentialDensityListCumul.size() && 
  						randResid> residentialDensityListCumul.get(residentialIterator)){
  					 if(randResid>residentialDensityListCumul.get(residentialIterator))
  						 residentialIterator++;
  				 }
  				 if(residentialIterator<coordList.size()){
  					 residence = new Coordinate(coordList.get(residentialIterator));
  					 randomResidences[residentialIterator]+=1;
  				 }
  			 }
  			 else{				  	 
	  			 int randResid = rand.nextInt(listResidentialCoordinates.size());
	  			 residence = listResidentialCoordinates.get(randResid);
	  			 int id = getIdNearestGridPoint(residence);
	  			 randomResidences[id]+=1;
	  			 listResidentialCoordinates.remove(randResid);
  			 }
  			 
  			 
  			 if(residence != null && activity !=null){
  				 trajectories.add(new Trajectory(residence, activity));
  			 }
  		 }
  	}
  	
    
    
    private static List<Coordinate> loadResidentialCoordinates(List<PopulationTile> populationTiles) {
    	List <Coordinate> listResidCoords = new ArrayList<>();
    	Random randLat = new Random();
    	Random randLon = new Random();
    	
		for(PopulationTile tile: populationTiles){
			for(int i=0; i<(int)Math.round(tile.getPopulation());i++){
				double lat = randLat.nextDouble()*(tile.getLatNorth()-tile.getLatSouth()) + tile.getLatSouth();
				double lon = randLon.nextDouble()*(tile.getLonEast()-tile.getLonWest()) + tile.getLonWest();
				Coordinate coord = new Coordinate(lon, lat);
				listResidCoords.add(coord);
			}
		}
		return listResidCoords;
    }



	private static int getIdNearestGridPoint(Coordinate coord) {
		int id = -1;
		int cpt = 0;
		double minDist = Integer.MAX_VALUE;
		for(Coordinate coordGrid : coordList)
		{
			double dist = Math.sqrt(Math.pow(coord.y-coordGrid.y,2)+Math.pow(coord.x-coordGrid.x, 2));
			//GISComputation.GPS2Meter(coord.y, coord.x, coordGrid.y, coordGrid.x); --> plus précis mais beaucoup plus lent. Pas besoin d'autant de précision ici
			if (dist < minDist){
				id = cpt;
				minDist = dist;
			}
			cpt++;
		}
		return id;
	}



	//Load population from dbf file
    private static List<PopulationTile> loadPopulation() 
    {
		List<PopulationTile> populations = new ArrayList<>();
		PopulationTile.setResolution(RESOLUTION);
	    Charset stringCharset = Charset.forName("Cp866");
	    File dbfFile = new File(INSEE_POPULATION_FILENAME);
	    if(!dbfFile.exists()){
	    	System.out.println("Missing INSEE population file...");
	    	return populations;
	    }
		DbfRecord rec;
			try (DbfReader reader = new DbfReader(dbfFile)) {
	            while ((rec = reader.read()) != null) 
	            {
	                rec.setStringCharset(stringCharset);
	                String id = rec.getString("idINSPIRE");
	                double nbHab = Double.valueOf(rec.getString("ind_c"));
	                double y_laea = Double.valueOf(id.substring(id.lastIndexOf("N")+1, id.lastIndexOf("E")))+RESOLUTION/2.0;
	                double x_laea = Double.valueOf(id.substring(id.lastIndexOf("E")+1))+RESOLUTION/2.0;
	            	EPSG3035Coordinate coords =  new EPSG3035Coordinate(x_laea, y_laea);
	            	Coordinate coordLatLon = coords.EPSG3035ToLatLong();
	            		
	            	if(isInBBox(coordLatLon))
	    				populations.add(new PopulationTile(coords,nbHab));
	            }
	            reader.close();
	        } catch (IOException e) {
				e.printStackTrace();
				return null;
			}
    	return populations;
   	}


	//Test if a coordinate point is in the bbox matrix
	private static boolean isInBBox(Coordinate coordLatLon) {
		boolean isInBBox = false;
		if(coordLatLon.y >= bboxMatrix[2] && coordLatLon.y <= bboxMatrix[1]
				&& coordLatLon.x >=bboxMatrix[3] && coordLatLon.x<= bboxMatrix[0])
			isInBBox=true;
		return isInBBox;
	}
    
    
	//Draw 2 heat map with random residences/activities
	private static void drawHeatMaps() {
		int nbLines = coordUTMMatrix[0].length;
		int nbCols = coordUTMMatrix.length;
		
		double[][] activityData = new double [nbLines][nbCols];
		double[][] residentialData = new double [nbLines][nbCols];
	    for (int i = 0; i < randomActivities.length; i++)
	    	activityData[nbLines-(i/nbCols)-1][i%nbCols] = randomActivities[i];
	    for (int i = 0; i < randomResidences.length; i++)
	    	residentialData[nbLines-(i/nbCols)-1][i%nbCols] = randomResidences[i];
	    
	    
	    HeatChart activityHeatMap = new HeatChart(activityData);
		activityHeatMap.setTitle("Activity heat map");
		 activityHeatMap.setHighValueColour(Color.RED);
		 activityHeatMap.setLowValueColour(Color.BLUE);
		 
		HeatChart residentialHeatMap = new HeatChart(residentialData);
		residentialHeatMap.setTitle("Residential heat map");
		residentialHeatMap.setHighValueColour(Color.RED);
		residentialHeatMap.setLowValueColour(Color.BLUE);
		
		try {
			File activityHMFile = new File(outputDirHeatMap+"activityHeatMap_"+cityName+".png");
			activityHMFile.mkdirs();
			activityHeatMap.saveToFile(activityHMFile);
			residentialHeatMap.saveToFile(new File(outputDirHeatMap+"residentialHeatMap_"+cityName+".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//Convert lists to cumulative lists
	private static void listsToCumulativeLists() {
		activityDensityListCumul =  activityDensityList;
		residentialDensityListCumul = residentialDensityList;
		for(int i=1; i< activityDensityListCumul.size(); i++)
			activityDensityListCumul.set(i, activityDensityListCumul.get(i)+activityDensityListCumul.get(i-1));
		for(int i=1; i< residentialDensityListCumul.size(); i++)
			residentialDensityListCumul.set(i, residentialDensityListCumul.get(i)+residentialDensityListCumul.get(i-1));
	}


	// Convert 3D coordinates matrix to coordinate list / 2D activity matrix to activity list / 2 D residential matrix to residential list
	private static void matrixToLists() {
    	coordList =  new ArrayList<Coordinate>();
    	activityDensityList =  new ArrayList<Double>();
    	residentialDensityList =  new ArrayList<Double>();
    	
    	UTMCoordinate bboxCenter = computeUTMZoneLetter();
    	
    	for (int j = 0; j < coordUTMMatrix[0].length; j++) {
    		for (int i = 0; i < coordUTMMatrix.length; i++) {
    			UTMCoordinate utm=new UTMCoordinate(coordUTMMatrix[i][j][0], coordUTMMatrix[i][j][1], bboxCenter.getZone(), bboxCenter.getLetter());
    			Coordinate latlong = utm.UTMToLatLong();
    			coordList.add(latlong);
    		}   
    	}
    	
    	if(activityDensityMatrix !=null && coordUTMMatrix[0].length == activityDensityMatrix.length 
    		&& coordUTMMatrix.length ==  activityDensityMatrix[0].length){
	    	for (int i = 0; i < activityDensityMatrix.length; i++) {
	    		for (int j = 0; j < activityDensityMatrix[0].length; j++) {
	    			activityDensityList.add(activityDensityMatrix[i][j]);
	    		}
	    	}
    	}
	
	
 		if(residentialDensityMatrix!=null && coordUTMMatrix[0].length == residentialDensityMatrix.length
 	    	&&  coordUTMMatrix.length == residentialDensityMatrix[0].length){
	    	for (int i = 0; i < residentialDensityMatrix.length; i++) {
	    		for (int j = 0; j < residentialDensityMatrix[0].length; j++) {
	    			residentialDensityList.add(residentialDensityMatrix[i][j]);
	             }
	         } 
    	}
	}


	//Compute the UTM coordinates of bbox center
	private static UTMCoordinate computeUTMZoneLetter() {
		double lat = (bboxMatrix[1]+bboxMatrix[2]) /2.0;
		double lon = (bboxMatrix[0]+bboxMatrix[3]) /2.0;
		UTMCoordinate utmCenter =  new UTMCoordinate(lat, lon);
		return utmCenter;
	}



	//Load a matrix from hadoop file
	private static void loadMatrix(int file_id, String groupName, String datasetName) {
		 int groupId = -1;
		 int dataspace_id = -1;
		 int dataset_id = -1;
		 long [] dims = null;
	     int rank = 0;
	     
		 //Open group and dataset
	     try {
	    	 if (file_id >= 0)
	    		 groupId = H5.H5Gopen(file_id, groupName, HDF5Constants.H5P_DEFAULT);
	    	 if (groupId >= 0)
	    		 dataset_id = H5.H5Dopen(groupId, datasetName, HDF5Constants.H5P_DEFAULT);
	     } catch (HDF5LibraryException | NullPointerException e) {
	    	 e.printStackTrace();
	    	 throw new RuntimeException(e);
	     }    


	     // Get dataspace and determine dataset parameters
	     try {
	    	 if (dataset_id >= 0){
	    		 dataspace_id = H5.H5Dget_space(dataset_id);
	    		 rank = H5.H5Sget_simple_extent_ndims ( dataspace_id );
		    	 dims= new long[rank];
		    	 H5.H5Sget_simple_extent_dims (dataspace_id , dims ,null );
	    	 }
	     }
	     catch (HDF5LibraryException | NullPointerException e) {
	    	 e.printStackTrace();
	    	 throw new RuntimeException(e);
	     }

	     
		 // Allocate matrix and read data
	     try {
	    	 if (dims != null){
	    		 switch(groupName) {
		    		 case BBOX_GROUPNAME:
		       			bboxMatrix = new double[(int) dims[0]];
		       			H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, bboxMatrix);
		       			break;
	       			case GRID_GROUPNAME:
	       				coordUTMMatrix = new double[(int) dims[0]][(int) dims[1]][(int) dims[2]];
	       				H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, coordUTMMatrix);
	       				break;
	       			case ACTIVITY_DENSITY_GROUPNAME:
	       				activityDensityMatrix = new double[(int) dims[0]][(int) dims[1]];
	       				H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, activityDensityMatrix);
	       				break;
	       			case RESIDENTIAL_DENSITY_GROUPNAME:
	       				residentialDensityMatrix = new double[(int) dims[0]][(int) dims[1]];
	       				H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, residentialDensityMatrix);
	       				break;
	       			default:
	       				throw new IllegalArgumentException("Invalid group name: " + groupName);
	    		 }
	    	 }
	     }
	     catch (Exception e) {
	    	 e.printStackTrace();
	    	 throw new RuntimeException(e);
	     }
       
	     // End access to data and release resources.
	     try {
	    	 if (dataset_id >= 0)
	    		 H5.H5Dclose(dataset_id);
	    	 if (dataspace_id >= 0)
	    		 H5.H5Sclose(dataspace_id);
	    	 if (groupId >= 0)
	    		 H5.H5Gclose(groupId);
	     }
	     catch (Exception e) {
	    	 e.printStackTrace();
	    	 throw new RuntimeException(e);
	     }
	}

	
	//Display a 2D matrix
	private static void displayMatrix(double[][] data) {
		//DecimalFormat format = new DecimalFormat("#,##0.0000000000000000000000");
        for (int i = 0; i < data.length; i++) {
            System.out.print(" [");
            for (int j = 0; j < data[0].length; j++) {
               // System.out.print(" " + format.format(data[i][j]));
            	System.out.print(" "+data[i][j]);
            }
            System.out.println("]");
        }
        System.out.println();	
	}
	
	//Display a 3D matrix
	private static void displayMatrix(double[][][] data) {
	    for (int i = 0; i < data.length; i++) {
	        for (int j = 0; j < data[0].length; j++) {
	            System.out.print(" [");
	        	for(int h=0; h<2; h++)
	        		System.out.print(" "+data[i][j][h]);
	            System.out.println("]");
	        }
	    }
	}
}