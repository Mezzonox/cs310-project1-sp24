package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/*// IMPORTS FOR TESTING PURPOSES
import java.io.FileWriter;
import java.io.IOException;*/

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    /*// METHOD FOR TESTING PURPOSES - TAKE OUT LATER
    public void writeJsonToFile(String jsonOutput, String jsonData) {
        
        try (FileWriter fileWriter = new FileWriter(jsonOutput)) {
            
            fileWriter.write(jsonData);
            System.out.println("JSON data written to " + jsonOutput);
            
        } catch (IOException e) {
            
            e.printStackTrace();
        }
    }*/
    
    public String convertCsvToJsonString(List<String[]> csv) {
        
        // Create outer JSON container
        JsonObject json = new JsonObject();
        
        // Create inner JSON containers
        JsonObject scheduletype = new JsonObject();
        JsonObject subject = new JsonObject();
        JsonObject course = new JsonObject();
        ArrayList<JsonObject> section = new ArrayList<>();
        
        // Set up CSV Iterator; Get Header Row
        Iterator<String[]> iterator = csv.iterator();
        String[] headerRow = iterator.next();
        HashMap<String, Integer> headers = new HashMap<>();
        
        for (int i = 0; i < headerRow.length; ++i) {
            
            headers.put(headerRow[i], i);
        }
        
        // Process CSV Records
        while (iterator.hasNext()) {
            
            String[] record = iterator.next();
            
            // Extract subject ID & number from "num" field
            String num = record[headers.get(NUM_COL_HEADER)];
            String[] numParts = num.split("\\s+");
            String subjectID = numParts[0];
            String number = numParts[1];
            
            // "scheduletype"
            String type = record[headers.get(TYPE_COL_HEADER)];
            String typeDescription = record[headers.get(SCHEDULE_COL_HEADER)];
            scheduletype.put(type, typeDescription);
        
            // "subject"
            String subjectName = record[headers.get(SUBJECT_COL_HEADER)];
            subject.put(subjectID, subjectName);
        
            // "course"
            JsonObject courseDetails = new JsonObject();
            courseDetails.put(SUBJECTID_COL_HEADER, subjectID);
            courseDetails.put(NUM_COL_HEADER, number);
            courseDetails.put(DESCRIPTION_COL_HEADER, record[headers.get(DESCRIPTION_COL_HEADER)]);
            courseDetails.put(CREDITS_COL_HEADER, Integer.valueOf(record[headers.get(CREDITS_COL_HEADER)])); // Convert from String to Integer
            String courseID = record[headers.get(NUM_COL_HEADER)];
            course.put(courseID, courseDetails);

            // "section"
            JsonObject sectionDetails = new JsonObject();
            sectionDetails.put(SECTION_COL_HEADER, record[headers.get(SECTION_COL_HEADER)]);
            sectionDetails.put(TYPE_COL_HEADER, record[headers.get(TYPE_COL_HEADER)]);
            sectionDetails.put(CRN_COL_HEADER, Integer.valueOf(record[headers.get(CRN_COL_HEADER)])); // Convert from String to Integer
            sectionDetails.put(NUM_COL_HEADER, number);
            sectionDetails.put(SUBJECTID_COL_HEADER, subjectID);
            sectionDetails.put(START_COL_HEADER, record[headers.get(START_COL_HEADER)]);
            sectionDetails.put(END_COL_HEADER, record[headers.get(END_COL_HEADER)]);
            sectionDetails.put(DAYS_COL_HEADER, record[headers.get(DAYS_COL_HEADER)]);
            sectionDetails.put(WHERE_COL_HEADER, record[headers.get(WHERE_COL_HEADER)]);
            
            // Split instructors into an array; Trim each element
            String[] instructorSplit = record[headers.get(INSTRUCTOR_COL_HEADER)].split(",");
            ArrayList<String> instructors = new ArrayList<>();
            
            for (String instructor : instructorSplit) {
                
                instructors.add(instructor.trim());
            }
            
            sectionDetails.put(INSTRUCTOR_COL_HEADER, instructors);
            section.add(sectionDetails);   
        }
        
        // Add each element to JSON object
        json.put("scheduletype", scheduletype);
        json.put("subject", subject);
        json.put("course", course);
        json.put("section", section);
        
        return Jsoner.serialize(json); 
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
        return "";
    }
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}