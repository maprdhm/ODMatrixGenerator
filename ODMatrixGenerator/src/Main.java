import java.awt.List;
import java.util.ArrayList;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Main {
    private static String FILENAME = "Villeurbanne.h5";
    private static String ACTIVITY_DENSITY_GROUPNAME = "kde_activity_30";
    private static String RESIDENTIAL_DENSITY_GROUPNAME = "kde_residential_30";
    private static String GRID_GROUPNAME = "xy_grid_30";
    private static String DATASETNAME = "block0_values";

    private static double [][] activityDensityMatrix;
    private static double [][] residentialDensityMatrix;
    private static double [][][] coordMatrix;
    private static ArrayList<LatLongCoordinate> coordList =  new ArrayList<LatLongCoordinate>();
    private static ArrayList<Double> activityDensityList =  new ArrayList();
    private static ArrayList<Double> residentialDensityList =  new ArrayList();

    
    public static void main(String[] args) {
    	loadData();
    }
    

	private static void loadData() {
	    int file_id = -1;

        // Open file
        try {
            file_id = H5.H5Fopen(FILENAME, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        activityDensityMatrix = loadDensity(file_id, ACTIVITY_DENSITY_GROUPNAME);
        residentialDensityMatrix = loadDensity(file_id, RESIDENTIAL_DENSITY_GROUPNAME);
        loadCoord(file_id);
        
        // Close access to data and release resources.
        try {
        	if (file_id >= 0)
                H5.H5Fclose(file_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
     /* displayMatrix(activityDensityMatrix);   
        displayMatrix(residentialDensityMatrix);   
        displayMatrix(coordMatrix);*/
        
        for (int i = 0; i < coordMatrix.length; i++) {
	        for (int j = 0; j < coordMatrix[0].length; j++) {
	        	UTMCoordinate utm=new UTMCoordinate(coordMatrix[i][j][0], coordMatrix[i][j][1], 31, 'T');
	        	LatLongCoordinate latlong=new LatLongCoordinate(utm);
	        	coordList.add(latlong);
	        }
        }
        
        double sum=0;
        for (int i = 0; i < activityDensityMatrix.length; i++) {
            for (int j = 0; j < activityDensityMatrix[0].length; j++) {
            	activityDensityList.add(activityDensityMatrix[i][j]);
            	sum+=activityDensityMatrix[i][j];
            }
        }
        System.out.println(sum);	
        System.out.println(activityDensityList.get(0));
        
       sum=0;
        for (int i = 0; i < residentialDensityMatrix.length; i++) {
            for (int j = 0; j < residentialDensityMatrix[0].length; j++) {
            	residentialDensityList.add(residentialDensityMatrix[i][j]);
            	sum+=residentialDensityMatrix[i][j];
            }
        }
        System.out.println(sum);	
        System.out.println(residentialDensityList.get(0));
        
        
	}
	
	
	
	private static void loadCoord(int file_id) {
		 int groupId = -1;
		 int dataspace_id = -1;
		 int dataset_id = -1;
		 long [] dims;
	     int rank = 0;
	     
		 //Open group
        try {
        	if (file_id >= 0)
       		 	groupId = H5.H5Gopen(file_id, GRID_GROUPNAME, HDF5Constants.H5P_DEFAULT);
   		} catch (HDF5LibraryException | NullPointerException e) {
   			e.printStackTrace();
   			throw new RuntimeException(e);
   		}    

        // Open dataset.
        try {
            if (groupId >= 0)
                dataset_id = H5.H5Dopen(groupId, DATASETNAME, HDF5Constants.H5P_DEFAULT);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Get dataspace
        try {
            if (dataset_id >= 0)
                dataspace_id = H5.H5Dget_space(dataset_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        
        /* Determine dataset parameters */
		try {
			rank = H5.H5Sget_simple_extent_ndims ( dataspace_id );
			dims= new long[rank];
			H5.H5Sget_simple_extent_dims (dataspace_id , dims ,null );
		} catch (HDF5LibraryException | NullPointerException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	       
        /* Create dataspace */
        try {
			dataspace_id = H5.H5Screate_simple ( rank , dims , null);
		} catch (NullPointerException | HDF5Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
  
       
        // Allocate two-dimensional arrays.
        coordMatrix = new double[(int) dims[0]][(int) dims[1]][(int) dims[2]];
        // Read data
        try {
            if (dataset_id >= 0)
                H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT, coordMatrix);
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

	
	
	private static double[][] loadDensity(int file_id, String groupName) {
		 int groupId = -1;
		 int dataspace_id = -1;
		 int dataset_id = -1;
		 long [] dims;
	     int rank = 0;
	     double [][] matrix;
		 
		 //Open group
        try {
        	if (file_id >= 0)
       		 	groupId = H5.H5Gopen(file_id, groupName, HDF5Constants.H5P_DEFAULT);
   		} catch (HDF5LibraryException | NullPointerException e) {
   			e.printStackTrace();
   			throw new RuntimeException(e);
   		}    

        // Open dataset.
        try {
            if (groupId >= 0)
                dataset_id = H5.H5Dopen(groupId, DATASETNAME, HDF5Constants.H5P_DEFAULT);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Get dataspace
        try {
            if (dataset_id >= 0)
                dataspace_id = H5.H5Dget_space(dataset_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        
        /* Determine dataset parameters */
		try {
			rank = H5.H5Sget_simple_extent_ndims ( dataspace_id );
			dims= new long[rank];
			H5.H5Sget_simple_extent_dims (dataspace_id , dims ,null );
		} catch (HDF5LibraryException | NullPointerException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	       
        /* Create dataspace */
        try {
			dataspace_id = H5.H5Screate_simple ( rank , dims , null);
		} catch (NullPointerException | HDF5Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

        // Allocate two-dimensional arrays.
        matrix = new double[(int) dims[0]][(int) dims[1]];

        // Read data
        try {
            if (dataset_id >= 0)
                H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT, matrix);
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
        return matrix;
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