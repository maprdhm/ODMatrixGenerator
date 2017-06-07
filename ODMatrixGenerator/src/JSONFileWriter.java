import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

/** Class to write json trajectories in file */
public class JSONFileWriter {
	
	public static void writeTrajectories(ArrayList<Trajectory> trajectories, String fileName) {	
	    try {
	        JsonWriter writer = new JsonWriter(new FileWriter(fileName));
	        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
	        writer.setIndent("  ");
	        writer.beginArray();
	        for(Trajectory t: trajectories)
	        	gson.toJson(t, Trajectory.class, writer);
	    	writer.endArray(); // ]
	    	writer.close();
	    }catch(IOException e){
			e.printStackTrace();
		}    
	}
}