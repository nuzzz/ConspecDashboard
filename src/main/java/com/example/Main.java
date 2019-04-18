/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Thread;
import java.nio.file.Path;
import java.nio.file.Paths;

import static javax.measure.unit.SI.KILOGRAM;
import javax.measure.quantity.Mass;
import org.jscience.physics.model.RelativisticModel;
import org.jscience.physics.amount.Amount;

import com.example.Model.Dog;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Files;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import biweekly.Biweekly;
import biweekly.ICalendar;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.reader.UniversalProjectReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Controller
@SpringBootApplication
public class Main {
	
	@Autowired
	private StorageService storageService;

	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

	@RequestMapping("/")
	String index() {
		return "index";
	}
	
	// dog generator
	@RequestMapping("/dog")
	String dog(Map<String, Object> model) {
		
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url("https://api.thedogapi.com/v1/images/search").build();
		String result = "";

		try {
			Response response = client.newCall(request).execute();
			result = response.body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//read json file data to String
		
		byte[] jsonData = result.getBytes();
		
		//create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);                 
        
		
		//convert json string to object
		Dog doggo;
		try {
			JsonNode rootNode = objectMapper.readTree(jsonData);
	        JsonNode urlNode = rootNode.get(0).get("url");
	        String url = urlNode.asText();
			
			//System.out.println("Dog Object\n"+ doggo);
			model.put("message3", url);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		model.put("message2", result);
		model.put("message4",  "Refresh for new doggo");
		return "dog";
	}
	
	@RequestMapping("/fileupload")
	public String fileupload(Map<String,Object> model){
		return "fileupload";
	}
	
	@RequestMapping(value = "/doUpload", method = RequestMethod.POST,
		    consumes = {"multipart/form-data"})
	public String upload(@RequestParam MultipartFile file) {
		return "redirect:/success.html";
	}	

	@RequestMapping("/dashboard")
	String dashboard(Map<String, Object> model) {

		String PROJECT_FILENAME = "Project Schedule 03-03-19.mpp";

		String message = "";
		ArrayList<ICalendar> calendars;
		ConspecProjectManager conspecPM = null;
		ProjectFile project = null;

		String configPath = "config.properties";
		Properties projProperties = new Properties();
		try {
			projProperties.load(new FileInputStream(configPath));
		} catch (IOException ioe) {
			message = "Unable to read file: " + ioe;
			System.out.println("Main|Unable to read file: " + ioe);
		}

		try {
			conspecPM = new ConspecProjectManager(PROJECT_FILENAME);
			project = conspecPM.getProjectFile();
		} catch (MPXJException e) {
			message = "Main|Failed to read project: " + e;
			System.out.println("Main|Failed to read project: " + e);
		}

		calendars = conspecPM.createProjectCalendars(project, "ICS");

		// Write overview.ics
		try {
			ICalendar overviewCal = calendars.get(0);
			File overview_file = new File(projProperties.getProperty("OVERVIEW_ICS"));
			Biweekly.write(overviewCal).go(overview_file);
			message += "Main|Succesfully written to overview file\n";
		} catch (IOException e1) {
			message = "Main|Unable to write to file: " + e1;
			System.out.println("Main|Unable to write to file: " + e1);
		}

		// Write reminder.ics
		try {
			ICalendar reminderCal = calendars.get(1);
			File reminder_file = new File(projProperties.getProperty("REMINDER_ICS"));
			Biweekly.write(reminderCal).go(reminder_file);
			message += "Main|Succesfully written to reminder file\n";
		} catch (IOException e1) {
			message = "Main|Unable to write to file: " + e1;
			System.out.println("Main|Unable to write to file: " + e1);
		}

		// Write weeklySummary.ics
		try {
			ICalendar weeklySummaryCal = calendars.get(2);
			File weeklySummary_file = new File(projProperties.getProperty("WEEKLY_SUMMARY_ICS"));
			Biweekly.write(weeklySummaryCal).go(weeklySummary_file);
			message += "Main|Succesfully written to weeklySummary file\n";
		} catch (IOException e1) {
			message = "Main|Unable to write to file: " + e1;
			System.out.println("Main|Unable to write to file: " + e1);
		}

		// Create a button that when pushed
		// 1. uploads mpp file to server.
		// 2. Open mpp file and load into memory
		// 3. create 3 calendar files, overview, reminder3 and mondaysummary
		// 4. Upload created calendar files to server
		// 5. for each calendar create a clickable downloadable link

		// Provide a tutorial of how to upload ics/csv files to outlook/google calendar
		// (image files hosted on database)

		//

		// Open file
		// for project in projects, get the list of project names and create buttons for
		// each

		// Get project list
		// ArrayList<String> projectList = getProjectList

		// for each project in project list, add a button
		model.put("loadButton", "Do something");
		model.put("globalmessage", message);

		return "dashboard";
	}

	@RequestMapping("/hello")
	String hello(Map<String, Object> model) {
		RelativisticModel.select();
		String energy = System.getenv().get("ENERGY");
		if (energy == null) {
			energy = "12 GeV";
		}
		Amount<Mass> m = Amount.valueOf("12 GeV").to(KILOGRAM);
		model.put("science", "E=mc^2: " + energy + " = " + m.toString());
		return "hello";
	}

	@RequestMapping("/db")
	String db(Map<String, Object> model) {
		try (Connection connection = dataSource.getConnection()) {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
			stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
			ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

			ArrayList<String> output = new ArrayList<String>();
			while (rs.next()) {
				output.add("Read from DB: " + rs.getTimestamp("tick"));
			}

			model.put("records", output);
			return "db";
		} catch (Exception e) {
			model.put("message", e.getMessage());
			return "error";
		}
	}

	@Bean
	public DataSource dataSource() throws SQLException {
		if (dbUrl == null || dbUrl.isEmpty()) {
			return new HikariDataSource();
		} else {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(dbUrl);
			return new HikariDataSource(config);
		}
	}

}
