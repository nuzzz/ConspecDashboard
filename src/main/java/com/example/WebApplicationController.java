package com.example;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.example.extensions.FileFormatException;
import com.example.helper.AmazonS3ClientService;
import com.example.model.Command;
import com.example.model.Dog;
import com.example.model.TodoistDue;
import com.example.model.TodoistTask;
import com.example.model.TodoistTempTask;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import biweekly.Biweekly;
import biweekly.ICalendar;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
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
	private RestTemplate restTemplate;

	@Autowired
	public WebApplicationController(WebApplicationStorageService storageService,
			WebApplicationTodoistService todoistService) {
		this.storageService = storageService;
		this.todoistService = todoistService;
	}

	public String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";

	private static final String API_URL = "https://todoist.com/api/v8/sync";
	private static final String API_AUTH = "https://todoist.com/oauth/authorize";
	private static final String API_TOKEN = "https://todoist.com/oauth/access_token";
	private static final String WEB_REDIRECT = "http://localhost:5000/callback";
	private static final String SCOPE = "data:read_write";// data:read //data:read_write
	private static final String RESPONSE_TYPE = "code";
	private static final int MAX_COMMANDS = 25;

	@Autowired
	private AmazonS3ClientService amazonS3ClientService;

	@RequestMapping("/upload")
	public String upload() {
		return "upload";
	}

//    @RequestMapping("uploadError")
//    public ModelAndView onUploadError(HttpServletRequest request) {
//        ModelAndView modelAndView = new ModelAndView("uploadPage");
//        modelAndView.addObject("error", request.getAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE));
//        return modelAndView;
//    }

	@RequestMapping("/")
	public String index(Map<String, Object> model) {

		// TODO: not implemented
		// uploadToS3();
		loadProjectData();
		// readAndSaveTodoistProject();
		// dailyUpdate(projectData);

		return "index";
	}

	// upload file

	// load all project tasks into memory with start and end date

//  @RequestMapping(value = "/upload", method = RequestMethod.POST)
//  public String onUpload(MultipartFile file) {
//      //System.out.println(file.getOriginalFilename());
//      this.amazonS3ClientService.uploadFileToS3Bucket(file, true);
//      this.amazonS3ClientService.listAllFiles();
//      return "upload";
//  }

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public Map<String, String> uploadFile(@RequestPart(value = "file") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		try {
			this.amazonS3ClientService.uploadFileToS3Bucket(file, true);
			response.put("message",
					"file [" + file.getOriginalFilename() + "] uploading request submitted successfully.");
		} catch (FileFormatException e) {
			response.put("message", "Error file needs to be Microsoft Project type (.mpp)");
		}

		return response;
	}

	public void loadProjectData() {
		Map<String, Long> tempToRealID = new HashMap<>();

		// Login //TODO: Unimplemented login

		// get time now
		LocalDate dateNow = LocalDate.now();

		// create project in todoist
		ProjectFile projectData = initProjectStuff();

		// save projectid to memory

		long projectID = createProject(projectData.getTasks().get(1).getName(), dateNow);

		todoistService.clearCommands();

		List<TodoistTempTask> taskList = todoistService.createTempTaskList(projectData, projectID);
		List<TodoistTempTask> taskListTillCurrentDate = todoistService.tillDateFilter(taskList,LocalDate.now());
		
		List<List<TodoistTempTask>> taskListSplitByMax = Lists.partition(taskListTillCurrentDate, MAX_COMMANDS);

		// TODO:
		// 1. On failure,
		// 2. If task already exist,

		// Store temptasklist into tempToRealID map (current only) [scheduled have no permanent id]
		for (List<TodoistTempTask> currentTaskList : taskListSplitByMax) {
			ResponseEntity<String> result = createTempTasks(currentTaskList);
			Map<String, Long> splitTaskIdMap = saveTaskIdFrom(result.getBody());
			tempToRealID.putAll(splitTaskIdMap);
			todoistService.clearCommands();
		}

		List<TodoistTask> createdTasks = createTodoistTaskList(taskListTillCurrentDate, tempToRealID);
		
		// Store scheduled into memory ensure it contains date => temp task mapping
		List<TodoistTempTask> scheduledTaskList = getScheduledTasks(taskList, tempToRealID);
		Map<LocalDate, List<TodoistTempTask>> scheduledTaskMap = scheduledTaskList.stream().collect(Collectors.groupingBy(TodoistTempTask::getStartDate));
		
		String jsonStr="";
		
		try {
			jsonStr = new ObjectMapper().writeValueAsString(scheduledTaskMap);
			//TODO: This is not working now 
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//	this.amazonS3ClientService.saveJsonTo("ScheduledTasks/scheduledTaskMap.json" , jsonStr, true);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//save json to file scheduledTaskMap.json
		File newFile = new File("scheduledTaskMap.json");
		try {
			Files.write(jsonStr.getBytes(), newFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String location = "ScheduledTasks/scheduledTaskMap.json";
		amazonS3ClientService.putFile(location, newFile);
		
//		S3Object s3object = s3client.getObject(bucketName, "picture/pic.png");
//		S3ObjectInputStream inputStream = s3object.getObjectContent();
//		FileUtils.copyInputStreamToFile(inputStream, new File("/Users/user/Desktop/hello.txt"));
		String filepath = "ScheduledTasks/scheduledTaskMap.json";
		try {
			String jsonStr2 = amazonS3ClientService.openFileAndGetJsonString(filepath);
			ObjectMapper mapper = new ObjectMapper();
			Map<LocalDate, List<TodoistTempTask>> map = mapper.readValue(jsonStr2, Map.class);
			System.out.println(map.values());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create loadScheduledTasks(scheduledtasklist,currentdate) and save new
		
		//loadScheduledTasks(scheduledTasks, LocalDate.now());
		// scheduled tasks

		// save user added tasks

		// OPTIONAL: redesign ui
	}

	private void loadScheduledTasks(List<TodoistTempTask> scheduledTasks, LocalDate now) {
		// TODO Auto-generated method stub
		
	}

	public List<TodoistTask> createTodoistTaskList(List<TodoistTempTask> tempTasks, Map<String, Long> tempToRealID) {
		List<TodoistTask> createdTasks = new ArrayList<>();

		for (TodoistTempTask task : tempTasks) {

			if (tempToRealID.containsKey(task.getTemp_id())) {
				TodoistTask newPermanentTask = new TodoistTask(tempToRealID.get(task.getTemp_id()), task.getTemp_id(),
						task.getData_id(), task.getContent(), task.getProject_id(), task.getStartDate(),
						task.getEndDate(), task.getDue(), task.getPriority(), task.getLabels());

				createdTasks.add(newPermanentTask);
			}

		}
		return createdTasks;
	}

	public List<TodoistTempTask> getScheduledTasks(List<TodoistTempTask> tempTasks, Map<String, Long> tempToRealID) {
		List<TodoistTempTask> scheduledTasks = new ArrayList<>();
		
		for( TodoistTempTask task : tempTasks ) {
			if(!tempToRealID.containsKey(task.getTemp_id())){
				scheduledTasks.add(task);
			}
				
		}
		return scheduledTasks;
	}

	public Map<String, Long> saveTaskIdFrom(String jsonString) {
		Map<String, Long> tempToRealID = new HashMap<>();

		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(jsonString);
			JsonNode temp_id_mapping = rootNode.get("temp_id_mapping");

			Iterator<Entry<String, JsonNode>> iter = temp_id_mapping.fields();
			while (iter.hasNext()) {
				Map.Entry<String, JsonNode> field = iter.next();
				String temp_id = field.getKey();
				Long real_id = field.getValue().asLong();

				tempToRealID.put(temp_id, real_id);
			}

			// temp_id_mapping.forEach(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tempToRealID;
	}

	public void showProjects() {
		this.amazonS3ClientService.listAllProjects();
	}

	// This returns a long project id only if the command was successful
	public long createProject(String projectName, LocalDate dateNow) {
		String projectTempId = todoistService
				.createTodoistProject(projectName + " " + dateNow.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

		ResponseEntity<String> result = sendCommands();

		String jsonString = result.getBody();
		ObjectMapper mapper = new ObjectMapper();
		long project_id = -1L;
		try {
			JsonNode rootNode = mapper.readTree(jsonString);
			project_id = rootNode.get("temp_id_mapping").get(projectTempId).asLong();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return project_id;
	}

	private ResponseEntity<String> createTempTasks(List<TodoistTempTask> taskList) {
		ResponseEntity<String> result = null;

		List<Command> commands = new ArrayList<>();

		for (TodoistTempTask task : taskList) {
			String commandID = UUID.randomUUID().toString();

			Map<String, Object> taskArguments = new HashMap<String, Object>();
			taskArguments.put("content", task.getContent());
			taskArguments.put("due", task.getDue());
			taskArguments.put("project_id", task.getProject_id());
			taskArguments.put("parent_id", task.getParent_id());
			taskArguments.put("child_order", task.getChild_order());

			Command addItemCommand = new Command("item_add", commandID, task.getTemp_id(), taskArguments);
			commands.add(addItemCommand);
		}

		todoistService.setCommands(commands);
		result = sendCommands();

		return result;
	}

	public ResponseEntity<String> sendCommands() {
		ResponseEntity<String> result = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Bearer " + todoistService.getAccessToken());

		HttpEntity<MultiValueMap<String, Object>> entity = null;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String commandsToJSON = objectMapper.writeValueAsString(todoistService.getCommands());

			MultiValueMap<String, Object> commandsMap = new LinkedMultiValueMap<>();
			commandsMap.add("commands", commandsToJSON);

			entity = new HttpEntity<MultiValueMap<String, Object>>(commandsMap, headers);
			result = restTemplate.postForEntity(todoistService.getSyncURL(), entity, String.class);
//			System.out.println("Entity: " + entity.getBody());
//			System.out.println("___________________");
//	
//			System.out.println("Result: " + result);
//			System.out.println("___________________");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		// Do any additional configuration here
		return builder.build();
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
