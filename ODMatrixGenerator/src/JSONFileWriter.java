import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JsonArray;
import org.json.simple.JsonObject;

/** Class to write json trajectories in file */
public class JSONFileWriter {

	public static void writeTrajectories(ArrayList<Trajectory> trajectories, String fileName){
		JsonArray trajectoriesArray = new JsonArray();
		for(Trajectory t : trajectories){
			JsonObject trajJSONObj = new JsonObject();
			JsonObject trajStartingJson = new JsonObject();
			JsonObject trajArrivalJson = new JsonObject();
			trajStartingJson.put("lat", t.getStarting().y);
			trajStartingJson.put("lon", t.getStarting().x);
			trajArrivalJson.put("lat", t.getArrival().y);
			trajArrivalJson.put("lon", t.getArrival().x);
			trajJSONObj.put("starting", trajStartingJson);
			trajJSONObj.put("arrival", trajArrivalJson);
			trajectoriesArray.add(trajJSONObj);
		}
		writeFile(trajectoriesArray, fileName);
	}
	
	
	
	public static void writeFile(JsonArray array, String fileName){
		try {
			FileWriter fileWriter = new FileWriter(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(array.toJson());
			bufferedWriter.flush();
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}