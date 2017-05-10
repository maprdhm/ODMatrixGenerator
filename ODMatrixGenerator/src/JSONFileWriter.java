import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JsonArray;
import org.json.simple.JsonObject;

public class JSONFileWriter {
	
	private static String fileName = "trajectories.json";

	public static void writeTrajectories(ArrayList<Trajectory> trajectories, String fileName){
		
		JsonArray trajectoriesArray = new JsonArray();
		
		for(Trajectory t : trajectories){
			JsonObject trajJSONObj = new JsonObject();
			JsonObject trajStartingJson = new JsonObject();
			JsonObject trajArrivalJson = new JsonObject();
			trajStartingJson.put("lat", t.getStarting().getLatitude());
			trajStartingJson.put("lon", t.getStarting().getLongitude());
			trajArrivalJson.put("lat", t.getArrival().getLatitude());
			trajArrivalJson.put("lon", t.getArrival().getLongitude());
			trajJSONObj.put("starting", trajStartingJson);
			trajJSONObj.put("arrival", trajArrivalJson);
			trajectoriesArray.add(trajJSONObj);
		}
		
		writeFile(trajectoriesArray);
	}
	
	
	
	public static void writeFile(JsonArray array){
		try {
			FileWriter fileWriter = new FileWriter("output\\"+fileName);
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

