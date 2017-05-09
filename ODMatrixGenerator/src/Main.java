import java.text.DecimalFormat;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Main {
    private static String FILENAME = "Lyon.h5";
    private static String DENSITY_GROUPNAME = "kde_activity_30";
    private static String GRID_GROUPNAME = "xy_grid_30";
    private static String DATASETNAME = "block0_values";

    private static double [][] densityMatrix;
    private static double [][][] coordMatrix;

    
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
        
        loadDensity(file_id);
       // displayMatrix(densityMatrix);
        loadCoord(file_id);
        
        
        for (int i = 0; i < coordMatrix.length; i++) {
            System.out.print(" [");
            for (int j = 0; j < coordMatrix[0].length; j++) {
            	for(int h=0; h<2; h++)
               // System.out.print(" " + format.format(data[i][j]));
            	System.out.print(" "+coordMatrix[i][j][h]);
            }
            System.out.println("]");
        }
        System.out.println();	
        
        // Close access to data and release resources.
        try {
        	if (file_id >= 0)
                H5.H5Fclose(file_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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

	
	
	private static void loadDensity(int file_id) {
		 int groupId = -1;
		 int dataspace_id = -1;
		 int dataset_id = -1;
		 long [] dims;
	        int rank = 0;
		 
		 //Open group
        try {
        	if (file_id >= 0)
       		 	groupId = H5.H5Gopen(file_id, DENSITY_GROUPNAME, HDF5Constants.H5P_DEFAULT);
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
        densityMatrix = new double[(int) dims[0]][(int) dims[1]];

        // Read data
        try {
            if (dataset_id >= 0)
                H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT, densityMatrix);
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
            for (int j = 0; j < data.length; j++) {
               // System.out.print(" " + format.format(data[i][j]));
            	System.out.print(" "+data[i][j]);
            }
            System.out.println("]");
        }
        System.out.println();	
	}
}