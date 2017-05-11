import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.rmi.CORBA.Tie;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Main {
    private static final String FILENAME = "input\\Lyon.h5";
    private static final String ACTIVITY_DENSITY_GROUPNAME = "kde_activity_30";
    private static final String RESIDENTIAL_DENSITY_GROUPNAME = "kde_residential_30";
    private static final String GRID_GROUPNAME = "xy_grid_30";
    private static final String DATASETNAME = "block0_values";
    private static String outputFile="output\\trajectories.txt";

    private static double [][] activityDensityMatrix;
    private static double [][] residentialDensityMatrix;
    private static double [][][] coordUTMMatrix;
    
    private static ArrayList<LatLongCoordinate> coordList;
    private static ArrayList<Double> activityDensityList;
    private static ArrayList<Double> residentialDensityList;
    
    private static ArrayList<Trajectory> trajectories;
    
    public static void main(String[] args) {
    	loadData();
    	/* displayMatrix(activityDensityMatrix);   
        displayMatrix(residentialDensityMatrix);   
        displayMatrix(coordMatrix);*/
    	matrixToLists();
    	listsToCumulativeLists();
    	
    	/*System.out.println(activityDensityList.get(activityDensityList.size()-1));
    	System.out.println(residentialDensityList.get(residentialDensityList.size()-1));
    	*/
    	generateTrajectories(1000);

    /*	for (int i=0;i<trajectories.size();i++)
    		System.out.println(trajectories.get(i).getStarting()+" "+trajectories.get(i).getArrival());*/
    	
    	//writeTrajectories();
    	
    	JSONFileWriter.writeTrajectories(trajectories, "trajectories.json");
    }
    
    
    
    private static void writeTrajectories() {
		FileWriter writer;
		try {
			writer = new FileWriter(outputFile);
			for(Trajectory t: trajectories) {
				  writer.write(t.toString());
				  writer.write("\n");
			}
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
	}



	private static void generateTrajectories(int nbTrajects) {  	
		trajectories = new ArrayList<>();
		 Random rand = new Random(987654321);
		 // Random rand = new Random(System.currentTimeMillis());
		 for(int i=0; i < nbTrajects; i++)
		 {
			 int activityIterator = 0;
			 int residentialIterator = 0;
			 double randActiv = rand.nextDouble();
			 double randResid = rand.nextDouble();

			 while(activityIterator< activityDensityList.size() &&
					 residentialIterator < residentialDensityList.size() && 
					 (randActiv>activityDensityList.get(activityIterator) || 
							 randResid> residentialDensityList.get(residentialIterator)))
			 {
				 if(randActiv>activityDensityList.get(activityIterator))
						 activityIterator++;
				 if(randResid>residentialDensityList.get(residentialIterator))
					 residentialIterator++;
			 }
			 LatLongCoordinate residence = new LatLongCoordinate(coordList.get(residentialIterator));
			 LatLongCoordinate activity =  new LatLongCoordinate(coordList.get(activityIterator));
			 trajectories.add(new Trajectory(residence, activity));
		 }
	}



	private static void listsToCumulativeLists() {
		for(int i=1; i< activityDensityList.size(); i++)
			activityDensityList.set(i, activityDensityList.get(i)+activityDensityList.get(i-1));
		for(int i=1; i< residentialDensityList.size(); i++)
			residentialDensityList.set(i, residentialDensityList.get(i)+residentialDensityList.get(i-1));
	}



	private static void matrixToLists() {
    	coordList =  new ArrayList<LatLongCoordinate>();
    	activityDensityList =  new ArrayList();
    	residentialDensityList =  new ArrayList();
    	
    	for (int j = 0; j < coordUTMMatrix[0].length; j++) {
    		for (int i = 0; i < coordUTMMatrix.length; i++) {
    			UTMCoordinate utm=new UTMCoordinate(coordUTMMatrix[i][j][0], coordUTMMatrix[i][j][1], 31, 'T');
    			LatLongCoordinate latlong=new LatLongCoordinate(utm);
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



	//Load data from .h5 file in matrix
	private static void loadData() {
		int file_id = -1;

        try {
            file_id = H5.H5Fopen(FILENAME, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        loadMatrix(file_id, ACTIVITY_DENSITY_GROUPNAME);
        loadMatrix(file_id, RESIDENTIAL_DENSITY_GROUPNAME);
        loadMatrix(file_id, GRID_GROUPNAME);
        
        try {
        	if (file_id >= 0)
                H5.H5Fclose(file_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
	}
	
	
	
	//Load a matrix
	private static void loadMatrix(int file_id, String groupName) {
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
	    		 dataset_id = H5.H5Dopen(groupId, DATASETNAME, HDF5Constants.H5P_DEFAULT);
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