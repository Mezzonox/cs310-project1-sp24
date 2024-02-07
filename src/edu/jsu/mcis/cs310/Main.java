package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import java.util.List;

public class Main {
    
    public static void main(String[] args) {
        
        ClassSchedule schedule = new ClassSchedule();
        
        try {
            
            // Get CSV/JSON Data
            
            List<String[]> csvOriginal = schedule.getCsv();
            JsonObject jsonOriginal = schedule.getJson();
            
            // Print Total Sections Found in CSV and JSON Data (should be equal)
            
            System.out.println("Sections Found (CSV): " + (csvOriginal.size() - 1));
            
            JsonArray sections = (JsonArray)jsonOriginal.get("section");
            System.out.println("Sections Found (JSON): " + sections.size());
            
            // THIS IS WHAT WAS HERE BEFORE USING FILES
            System.out.println(schedule.convertCsvToJsonString(csvOriginal));
            
            // Convert CSV to JSON
            String jsonOutput = schedule.convertCsvToJsonString(csvOriginal);

            // Write JSON data to a file FOR TESTING - TAKE OUT LATER
            //schedule.writeJsonToFile("output.json", jsonOutput);
            
            //System.out.println(schedule.convertJsonToCsvString(jsonOriginal));
            
        }
        catch (Exception e) { e.printStackTrace(); }
            
    }
    
}