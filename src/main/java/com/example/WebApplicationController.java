package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.extensions.StorageFileNotFoundException;
import com.example.helper.WeeklySummaryHelper;
import com.example.model.Dog;
import com.example.model.Due;
import com.example.model.TodoistProject;
import com.example.model.TodoistTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoist.pojo.Item;
import com.todoist.pojo.Project;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Summary;
import biweekly.util.Duration;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Controller
@ComponentScan(basePackages = { "com.example" })
public class WebApplicationController {

	private final WebApplicationStorageService storageService;
	private final WebApplicationTodoistService todoistService;

	@Autowired
	private DataSource dataSource;

	@Value("${spring.datasource.url}")
	private String dbUrl;

	static String cid = "a0926c2f533a448bb14213e5bae8a964";
	String csecret = "3534a321bc474ce18ee8134aae2720f1";

	@Autowired
	public WebApplicationController(WebApplicationStorageService storageService,
			WebApplicationTodoistService todoistService) {
		this.storageService = storageService;
		this.todoistService = todoistService;

	}

	private final static String API_URL = "https://todoist.com/api/v8/sync";
	private final static String API_AUTH = "https://todoist.com/oauth/authorize";
	private final static String API_TOKEN = "https://todoist.com/oauth/access_token";
	private final static String WEB_REDIRECT = "http://localhost:5000/callback";
	private static final String SCOPE = "data:read_write";// data:read //data:read_write
	private static final String RESPONSE_TYPE = "code";

	
	@RequestMapping("/")
	String index() {

		/* This method does not work due to sending too many request at once.
		*/
		WeeklySummaryHelper wsh = new WeeklySummaryHelper();
		ProjectFile projectData = initProjectStuff();
		// Create project
		long project_id = createProject("Project Schedule 03-03-19.mpp");
		// compare with datenow to choose which tasks to add into bennington setapak
		LocalDate dateNow = LocalDate.now();

		for (Task task : projectData.getTasks()) {
			// This task is an activity if it fulfills 3 conditions:
			// 1. If taskname does not contain milestone and
			// 2. If task has a parent task and
			// 3. If task has no children task
			if (!task.getName().contains("Milestone") && task.getParentTask() != null
					&& task.getChildTasks().size() == 0) {

				if (!wsh.convertToLocalDateViaInstant(task.getStart()).isAfter(dateNow)) {
					addNewTask(task, project_id);
				}
			}
		}
		return "index";
	}
	// upload file

	// load all project tasks into memory with start and end date

	public void runAutomatedChecklist() {// ArrayList<Task> projectData) {

		// get time now
		LocalDate dateNow = LocalDate.now();

		// create project in todoist

		// TODO: replace PROJECT_NAME and TIME_NOW
		createProject("PROJECT_NAME TIME_NOW");

		// save projectid to memory

		// add task to project, if exists
	}

	@RequestMapping(value = "/todoist/revoke", method = RequestMethod.POST)
	private ModelAndView revokeToken() {

		String REVOKE_URL = "https://todoist.com/api/access_tokens/revoke";
		String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";
		// https://beta.todoist.com/API/v8/projects
		// https://todoist.com/api/v8/sync
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("todoist.com")
				.path("/api/access_tokens/revoke").queryParam("client_id", cid).queryParam("token", ACCESS_TOKEN);

		String revokeURL = uriBuilder.build().encode().toUriString();

		return new ModelAndView("redirect:" + revokeURL);
	}

	public ProjectFile initProjectStuff() {
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
		return project;
	}
//
//	private void createProjectv2() {
//		String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";
//
//		UUID newID = UUID.randomUUID();
//		ResponseEntity<String> result = new ResponseEntity<String>(HttpStatus.NOT_FOUND);
//		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("todoist.com")
//				.path("/API/v8/projects");
//	}
//	
	private ResponseEntity<String> addNewTask(Task task, long project_id) {
		ProjectFile projectData = initProjectStuff();

		String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";
		UUID newID = UUID.randomUUID();
		Set<Integer> newLabelList = new HashSet<Integer>();

		WeeklySummaryHelper wsh = new WeeklySummaryHelper();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String date_str = wsh.convertToLocalDateViaInstant(task.getFinish()).format(formatter);

		Due due = new Due(date_str, "", "", "", false);

		ResponseEntity<String> result = new ResponseEntity<String>(HttpStatus.OK);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("beta.todoist.com")
				.path("/API/v8/tasks");

		String postTasksURL = uriBuilder.build().encode().toUriString();

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Request-Id", newID.toString());
		headers.set("Authorization", "Bearer " + ACCESS_TOKEN);

		TodoistTask todoistTask = null;
		try {
			// project(id,name,order)
			// TODO: missing due and indent
			todoistTask = new TodoistTask(123, false, task.getName(), project_id, -1, newLabelList, 0, due, 1);

			HttpEntity<TodoistTask> entity = new HttpEntity<>(todoistTask, headers);
			System.out.println(entity);
			result = restTemplate.postForEntity(postTasksURL, entity, String.class);
			System.out.println(result);
		} catch (NumberFormatException e) {
			System.out.println("failed to parse newId in addNewTask function");
		}
		return result;
	}

	private static long createProject(String projectName) {
		String endpoint = "https://beta.todoist.com/API/v8/projects";
		String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";

		UUID newID = UUID.randomUUID();
		ResponseEntity<String> result = new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("beta.todoist.com")
				.path("/API/v8/projects");

		String postProjectsURL = uriBuilder.build().encode().toUriString();

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Request-Id", newID.toString());
		headers.set("Authorization", "Bearer " + ACCESS_TOKEN);
		long project_id = -1;
		TodoistProject newProject = null;
		try {
			// project(id,name,order)

			newProject = new TodoistProject(project_id, false, projectName, 1, 1, 2);
			HttpEntity<TodoistProject> entity = new HttpEntity<>(newProject, headers);
			System.out.println(entity);
			result = restTemplate.postForEntity(postProjectsURL, entity, String.class);
			// System.out.println(result.getBody());
			System.out.println(result);
		} catch (NumberFormatException e) {
			System.out.println("failed to parse project id in createProject function");
		}
		String jsonString = result.getBody();
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(jsonString);
			project_id = Long.parseLong(actualObj.get("id").asText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return project_id;
	}

	private static ResponseEntity<String> getProjects() {
		String PROJECTS_URL = "https://beta.todoist.com/API/v8/projects";
		String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";
		// https://beta.todoist.com/API/v8/projects
		// https://todoist.com/api/v8/sync
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("beta.todoist.com")
				.path("/API/v8/projects").queryParam("token", ACCESS_TOKEN);

		String getProjectsURL = uriBuilder.build().encode().toUriString();

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		ResponseEntity<String> result = restTemplate.exchange(getProjectsURL, HttpMethod.GET, entity, String.class);

		return result;
	}

	@RequestMapping(value = "/todoist/projects", method = RequestMethod.GET)
	public String todoistProjects(Map<String, String> model) {
		ResponseEntity<String> result = getProjects();
		model.put("message", result.getBody());
		return "/todoist/projects";
	}

	@RequestMapping(value = "/oauthTodo", method = RequestMethod.GET)
	public ModelAndView sendOAuth() {

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("todoist.com")
				.path("/oauth/authorize").queryParam("reponse_type", RESPONSE_TYPE).queryParam("client_id", cid)
				.queryParam("scope", SCOPE);

		String authUrl = uriBuilder.build().encode().toUriString();

		return new ModelAndView("redirect:" + authUrl);
	}

	@RequestMapping(value = "/callback", method = RequestMethod.GET)
	public String callbackTodoist(Map<String, Object> model, @RequestParam(value = "code") String code) {
		System.out.println(code);
		model.put("message", code);

		return "callback";
	}

	// dog generator
	@RequestMapping("/dog")
	public String dog(Map<String, Object> model) {

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

		// read json file data to String

		byte[] jsonData = result.getBytes();

		// create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// convert json string to object
		Dog doggo;
		try {
			JsonNode rootNode = objectMapper.readTree(jsonData);
			JsonNode urlNode = rootNode.get(0).get("url");
			String url = urlNode.asText();

			// System.out.println("Dog Object\n"+ doggo);
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
		model.put("message4", "Refresh for new doggo");
		return "dog";
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
