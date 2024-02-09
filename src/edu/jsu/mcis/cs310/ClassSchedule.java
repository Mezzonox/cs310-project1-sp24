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
   
        // Create the StringWriter & CSV Writer to write CSV data
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        // Define CSV headers
        String[] headers = {CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER, DESCRIPTION_COL_HEADER, SECTION_COL_HEADER,
            TYPE_COL_HEADER, CREDITS_COL_HEADER, START_COL_HEADER,
            END_COL_HEADER, DAYS_COL_HEADER, WHERE_COL_HEADER, SCHEDULE_COL_HEADER, INSTRUCTOR_COL_HEADER};
        
        // Write headers to CSV
        csvWriter.writeNext(headers);
        
        // Create JsonArray to hold "section" data
        JsonArray sections = (JsonArray) json.get(SECTION_COL_HEADER);
        
        // Iterate through each section in the array
        for (Object sectionObject : sections) {
            
            JsonObject sectionJson = (JsonObject) sectionObject;
            List<String> row = new ArrayList<>();
            
            // Concatenate subject ID and Course Number
            String courseNum = sectionJson.get(SUBJECTID_COL_HEADER) + " " + sectionJson.get(NUM_COL_HEADER);
            
            // Get the "course" details from the JSON Object; Get the nested JSON Object for the current course using the course ID
            JsonObject courseObject = (JsonObject) json.get("course");
            JsonObject courseDetails = (JsonObject) courseObject.get(courseNum);

            // Get the "description" and "credits" fields from the course details
            String description = (String) courseDetails.get("description");
            String credits = String.valueOf(courseDetails.get("credits"));
            
            // Get the "scheduletype" JSON object from the main JSON object; Get the schedule type description
            JsonObject scheduleTypeObject = (JsonObject) json.get("scheduletype");
            String type = (String) sectionJson.get(TYPE_COL_HEADER);
            String schedule = (String) scheduleTypeObject.get(type);
            
            // Add data to row
            row.add(String.valueOf(sectionJson.get(CRN_COL_HEADER)));
            row.add((String) ((JsonObject) json.get("subject")).get(sectionJson.get(SUBJECTID_COL_HEADER).toString()));
            row.add(courseNum);
            row.add(description);
            row.add(String.valueOf(sectionJson.get(SECTION_COL_HEADER)));
            row.add(String.valueOf(sectionJson.get(TYPE_COL_HEADER)));
            row.add(credits);
            row.add(String.valueOf(sectionJson.get(START_COL_HEADER)));
            row.add(String.valueOf(sectionJson.get(END_COL_HEADER)));
            row.add(String.valueOf(sectionJson.get(DAYS_COL_HEADER)));
            row.add(String.valueOf(sectionJson.get(WHERE_COL_HEADER)));
            row.add(schedule);
          
            // Convert instructors list to a comma-separated string; Add instructors to row
            List<String> instructors = (List<String>) sectionJson.get(INSTRUCTOR_COL_HEADER);
            String instructorString = String.join(", ", instructors);
            row.add(instructorString);
            
            // Write row to CSV
            csvWriter.writeNext(row.toArray(new String[0]));
        }
        
        return writer.toString();  
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