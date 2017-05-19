import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.tc33.jheatchart.HeatChart;

import com.vividsolutions.jts.geom.Coordinate;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Main {
    private static final String FILENAME = "input\\Villeurbanne.h5";
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
    
    
    public static void main(String[] args) {
    	cityName = FILENAME.substring(FILENAME.lastIndexOf("\\")+1, FILENAME.lastIndexOf("."));
    	loadData();
    	/* displayMatrix(activityDensityMatrix);   
        displayMatrix(residentialDensityMatrix);   
        displayMatrix(coordMatrix);*/
    	matrixToLists();
    	listsToCumulativeLists();
    	
    	generateTrajectories(100000);
    	outputFileJson+= cityName +".json";
    	JSONFileWriter.writeTrajectories(trajectories, outputFileJson);
    	
    	drawHeatMaps();
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


	//Generate random trajectories
	private static void generateTrajectories(int nbTrajects) {  	
		trajectories = new ArrayList<>();
		randomResidences = new int[coordList.size()];
		randomActivities = new int[coordList.size()];
		 Random rand = new Random(987654321);
		// Random rand = new Random(System.currentTimeMillis());
		 for(int i=0; i < nbTrajects; i++)
		 {
			 int activityIterator = 0;
			 int residentialIterator = 0;
			 double randActiv = rand.nextDouble();
			 double randResid = rand.nextDouble();

			 while(activityIterator< activityDensityListCumul.size() &&
					 residentialIterator < residentialDensityListCumul.size() && 
					 (randActiv>activityDensityListCumul.get(activityIterator) || 
							 randResid> residentialDensityListCumul.get(residentialIterator)))
			 {
				 if(randActiv>activityDensityListCumul.get(activityIterator))
						 activityIterator++;
				 if(randResid>residentialDensityListCumul.get(residentialIterator))
					 residentialIterator++;
			 }
			 if(residentialIterator<coordList.size() && activityIterator < coordList.size()){
				 Coordinate residence = new Coordinate(coordList.get(residentialIterator));
				 Coordinate activity =  new Coordinate(coordList.get(activityIterator));
				 randomResidences[residentialIterator]+=1;
				 randomActivities[activityIterator]+=1;
				 trajectories.add(new Trajectory(residence, activity));
			 }
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
    	if(coordUTMMatrix[0].length == activityDensityMatrix.length 
    		&& coordUTMMatrix[0].length == residentialDensityMatrix.length
    		&& coordUTMMatrix.length ==  activityDensityMatrix[0].length
    		&&  coordUTMMatrix.length == residentialDensityMatrix[0].length)
    	{
	    	for (int j = 0; j < coordUTMMatrix[0].length; j++) {
	    		for (int i = 0; i < coordUTMMatrix.length; i++) {
	    			UTMCoordinate utm=new UTMCoordinate(coordUTMMatrix[i][j][0], coordUTMMatrix[i][j][1], bboxCenter.getZone(), bboxCenter.getLetter());
	    			Coordinate latlong = utm.UTMToLatLong();
	    			coordList.add(latlong);
	    		}   
	    	}
	    	
	    	for (int i = 0; i < activityDensityMatrix.length; i++) {
	    		for (int j = 0; j < activityDensityMatrix[0].length; j++) {
	    			activityDensityList.add(activityDensityMatrix[i][j]);
	    		}
	    	}
	
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



	//Load needed data from .h5 file in matrix
	private static void loadData() {
		int file_id = -1;

        try {
            file_id = H5.H5Fopen(FILENAME, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        loadMatrix(file_id, BBOX_GROUPNAME, BBOX_DATASETNAME);
        loadMatrix(file_id, ACTIVITY_DENSITY_GROUPNAME, DATASETNAME);
        loadMatrix(file_id, RESIDENTIAL_DENSITY_GROUPNAME, DATASETNAME);
        loadMatrix(file_id, GRID_GROUPNAME, DATASETNAME);
        
        try {
        	if (file_id >= 0)
                H5.H5Fclose(file_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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