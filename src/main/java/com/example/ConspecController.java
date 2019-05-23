package com.example;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.extensions.FileFormatException;
import com.example.helper.S3ClientService;
import com.example.model.Command;
import com.example.model.TodoistProject;
import com.example.model.TodoistTask;
import com.example.model.TodoistTempTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.reader.UniversalProjectReader;

@Controller
public class ConspecController {

	private final static Logger LOGGER = Logger.getLogger(ConspecController.class.getName());

	private final ConspecTodoistService todoistService;
	private final S3ClientService s3ClientService;

	@Autowired
	private DataSource dataSource;

	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	public ConspecController(ConspecTodoistService todoistService, S3ClientService s3ClientService) {
		this.todoistService = todoistService;
		this.s3ClientService = s3ClientService;
	}

	private static final String SCOPE = "data:read_write";
	private static final String RESPONSE_TYPE = "code";
	private static final int MAX_COMMANDS = 25;
	//private static final String SCHEDULED_TASK_FILENAME = "scheduledTaskList123.json";
	//private static final String SCHEDULED_TASK_S3_LOCATION = "ScheduledTasks/scheduledTaskList123.json";
	private static final String SCHEDULED_TASK_S3_DIRECTORY = "ScheduledTasks/";
	private static final String SCHEDULED_TASK_FILENAME = "scheduledTaskList";
	private static final String JSON_FILE_EXT = ".json";

	// Configure tomcat to set maxsize of upload files before processing
	@Bean
	public TomcatServletWebServerFactory containerFactory() {
		return new TomcatServletWebServerFactory() {
			protected void customizeConnector(Connector connector) {
				int maxSize = -1;
				super.customizeConnector(connector);
				connector.setMaxPostSize(maxSize);
				connector.setMaxSavePostSize(maxSize);
				if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {

					((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(maxSize);
					logger.info("Set MaxSwallowSize " + maxSize);
				}
			}
		};
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		// Do any additional configuration here
		return builder.build();
	}


	@RequestMapping("/")
	public String index(Map<String, Object> model) {
		return "index";
	}

	@RequestMapping("/projects")
	public String projects(Map<String, Object> model) {
		List<String> projects = new ArrayList<>();
		
		projects = s3ClientService.fileListFromS3Directory(s3ClientService.getProjectDirectory());
		model.put("projects", projects);
		
		return "projects";
	}

	@RequestMapping(value="/loadthisproject", method = RequestMethod.POST)
	public String loadThisProject(Map<String, Object> model, @RequestPart(value="project") String project){
		//project = "Projects/%file_name%"
		//extract filename
		String file_name = getFilenameFromS3Path(project);
		File f = this.s3ClientService.getFileFromS3(project, file_name);
		long projectID = loadProjectData(file_name);
		
		
		model.put("message", "The project id is " + String.valueOf(projectID));
		
		f.delete();
		
		return "loadthisproject";
	}

	@RequestMapping("/runschedule")
	public String runSchedule(Map<String, Object> model) {
		List<ResponseEntity<String>> allResults = runProjectScheduledTaskManager();
		for(ResponseEntity<String> result : allResults) {
			if(model.get("message")!=null) {
				model.put("message", model.get("message") + "\n" +result.getBody());
			}else {
				model.put("message", result.getBody());
			}
		}
		
		return "runschedule";
	}

	@RequestMapping("/upload")
	public String upload() {
		return "upload";
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public Map<String, String> uploadFile(@RequestPart(value = "file") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		try {
			this.s3ClientService.uploadFileToS3Bucket(file, true);
			response.put("message", "File [" + file.getOriginalFilename() + "] uploaded successfully.");
		} catch (FileFormatException e) {
			response.put("message", "Error file needs to be Microsoft Project type. (.mpp)");
		}
		return response;
	}

	@RequestMapping(value = "/oauthTodo", method = RequestMethod.GET)
	public ModelAndView sendOAuth() {

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("todoist.com")
				.path("/oauth/authorize").queryParam("reponse_type", RESPONSE_TYPE)
				.queryParam("client_id", todoistService.getClientID()).queryParam("scope", SCOPE);

		String authUrl = uriBuilder.build().encode().toUriString();

		return new ModelAndView("redirect:" + authUrl);
	}

	@RequestMapping(value = "/callback", method = RequestMethod.GET)
	public String callbackTodoist(Map<String, Object> model, @RequestParam(value = "code") String code) {
		model.put("message", code);
		return "callback";
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/todoist/revoke", method = RequestMethod.POST)
	private ModelAndView revokeToken() {

		String REVOKE_URL = "https://todoist.com/api/access_tokens/revoke";
		String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";
		// https://beta.todoist.com/API/v8/projects
		// https://todoist.com/api/v8/sync
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("todoist.com")
				.path("/api/access_tokens/revoke").queryParam("client_id", todoistService.getClientID())
				.queryParam("token", ACCESS_TOKEN);

		String revokeURL = uriBuilder.build().encode().toUriString();

		return new ModelAndView("redirect:" + revokeURL);
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

	public ProjectFile initProjectFrom(String projectFilename) {
		ProjectReader reader = new UniversalProjectReader();
		ProjectFile projectData = new ProjectFile();
		try {
			projectData = reader.read(projectFilename);
		} catch (MPXJException e) {
			LOGGER.severe("Failed to read project file: " + e);
		}
		return projectData;
	}

	// String projectFileLocation is in s3
	public ProjectFile loadProjectFile(String projectFileLocation, String filename) {
		File f = s3ClientService.getFileFromS3(projectFileLocation, filename);
		ProjectFile projectData = initProjectFrom(f.getName());

		return projectData;
	}

	// This function should run on button click of project name depending on file
	// uploaded to file directory in s3.
	public long loadProjectData(String project_filename) {
		Map<String, Long> tempToRealID = new HashMap<>();

		// Login //TODO: Unimplemented login
		LocalDate dateNow = LocalDate.now();

		// create project in todoist
		ProjectFile projectData = initProjectFrom(project_filename);

		// save projectid to memory
		long projectID = createProject(projectData.getTasks().get(1).getName(), dateNow);

		List<TodoistTempTask> taskList = todoistService.createTempTaskList(projectData, projectID);
		List<TodoistTempTask> taskListTillCurrentDate = todoistService.tillDateFilter(taskList, LocalDate.now());

		List<List<TodoistTempTask>> taskListSplitByMax = Lists.partition(taskListTillCurrentDate, MAX_COMMANDS);

		// TODO:
		// 1. On failure,
		// 2. If task already exist,

		// Store temptasklist into tempToRealID map (current only) [scheduled have no
		// permanent id]
		for (List<TodoistTempTask> currentTaskList : taskListSplitByMax) {
			ResponseEntity<String> result = createTempTasks(currentTaskList);
			Map<String, Long> splitTaskIdMap = saveTaskIdFrom(result.getBody());
			tempToRealID.putAll(splitTaskIdMap);
		}

		// List<TodoistTask> createdTasks =
		// createTodoistTaskList(taskListTillCurrentDate, tempToRealID);

		// Store scheduled into memory ensure it contains date => temp task mapping
		List<TodoistTempTask> scheduledTaskList = getScheduledTasks(taskList, tempToRealID);

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.writerWithDefaultPrettyPrinter();
			String jsonStr = mapper.writeValueAsString(scheduledTaskList);

			// Create file locally
			String scheduledFilename = SCHEDULED_TASK_FILENAME+projectID+JSON_FILE_EXT;
			File newFile = new File(scheduledFilename);
			Files.write(jsonStr.getBytes(), newFile);

			// Place file into s3
			// scheduled_task_dir + scheduled_task_filename + project_id + json_extension
			String scheduledFileLocation = SCHEDULED_TASK_S3_DIRECTORY + scheduledFilename;
			s3ClientService.putFile(scheduledFileLocation, newFile);
		
			// Delete local file
			newFile.delete();
		} catch (JsonProcessingException jsonProcessingException) {
			LOGGER.severe("JSON Processing Exception: " + jsonProcessingException );
		} catch (IOException ioException) {
			LOGGER.severe("Input Ouput Exception: " + ioException);
		}
		return projectID;
	}

	public List<TodoistTask> createTodoistTaskList(List<TodoistTempTask> tempTasks, Map<String, Long> tempToRealID) {
		List<TodoistTask> createdTasks = new ArrayList<>();

		for (TodoistTempTask task : tempTasks) {

			if (tempToRealID.containsKey(task.getTemp_id())) {
				TodoistTask newPermanentTask = new TodoistTask(tempToRealID.get(task.getTemp_id()), task.getTemp_id(),
																				task.getData_id(), task.getContent(), 
																				task.getProject_id(), task.getStartDate(),
																				task.getEndDate(), task.getDue(), 
																				task.getPriority(), task.getLabels());
				createdTasks.add(newPermanentTask);
			}
		}
		return createdTasks;
	}

	public List<TodoistTempTask> getScheduledTasks(List<TodoistTempTask> tempTasks, Map<String, Long> tempToRealID) {
		List<TodoistTempTask> scheduledTasks = new ArrayList<>();

		for (TodoistTempTask task : tempTasks) {
			if (!tempToRealID.containsKey(task.getTemp_id())) {
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


	// This returns a long project id only if the command was successful
	public long createProject(String projectName, LocalDate dateNow) {
		String projectTempId = todoistService
				.createTodoistProject(projectName + " " + dateNow.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

		ResponseEntity<String> result = sendTodoistCommands();

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
		result = sendTodoistCommands();

		return result;
	}

	public ResponseEntity<String> sendTodoistCommands() {
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			todoistService.clearCommands();
		}
		return result;
	}

	/******************************************************************************************************
	 * ScheduledTasks code begins here
	 * @return 
	 * 
	 * 
	 ******************************************************************************************************/

	public List<ResponseEntity<String>> runProjectScheduledTaskManager() {
		//Get all files from scheduledTasks directory
		List<String> scheduledTasksFileList = s3ClientService.fileListFromS3Directory(SCHEDULED_TASK_S3_DIRECTORY);
		
		List<ResponseEntity<String>> allResults = new ArrayList<>();
		
		for(String fullFilePath : scheduledTasksFileList) {
			ResponseEntity<String> result = runScheduledTasksFor(fullFilePath);
			allResults.add(result);
		}
		return allResults;
	}
	
	@Async
	public ResponseEntity<String> runScheduledTasksFor(String s3FilePath){
		ResponseEntity<String> result = null;
		String file = getFilenameFromS3Path(s3FilePath);
		String jsonString = s3ClientService.getJsonStringFromS3(SCHEDULED_TASK_S3_DIRECTORY + file);
		if (!jsonString.equals("[]")) {
			// Put tasks into taskList
			List<TodoistTempTask> scheduledTaskList = new ArrayList<>();
			LocalDate currentDate = LocalDate.now();
			try {
				scheduledTaskList = jsonArrayToObjectList(jsonString, TodoistTempTask.class);
			} catch (IOException error) {
				LOGGER.severe("IO Exception occurred: " + error);
			}

			// Collect tasks to be added.
			ArrayList<TodoistTempTask> addTheseTasks = new ArrayList<>();
			int taskCount = 0;
			for (TodoistTempTask task : scheduledTaskList) {
				if (!task.getStartDate().isAfter(currentDate)) {
					addTheseTasks.add(task);
					taskCount++;
				}
			}
			// remove added tasks from scheduled tasks
			scheduledTaskList.removeAll(addTheseTasks);

			// reupload to s3 removed task list back into scheduled
			uploadTasklistToS3(scheduledTaskList, s3FilePath);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LOGGER.info("added " + taskCount + " tasks on " + currentDate.format(dtf));
			todoistService.addTasksToTodoist(addTheseTasks);
			result = sendTodoistCommands();
			
		} else {
			LOGGER.info("No tasks added because jsonString is empty");
		}
		return result;
	}

	public void uploadTasklistToS3(List<TodoistTempTask> taskList, String s3FilePath) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.writerWithDefaultPrettyPrinter();
			String jsonStr = mapper.writeValueAsString(taskList);
			File newFile = new File(getFilenameFromS3Path(s3FilePath));
			Files.write(jsonStr.getBytes(), newFile);
			s3ClientService.putFile(s3FilePath, newFile);
			newFile.delete();
		} catch (JsonProcessingException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // uploadToS3

	private String getFilenameFromS3Path(String s3FilePath) {
		return s3FilePath.substring(s3FilePath.lastIndexOf("/") +1);
	}
	
	// Credits: https://stackoverflow.com/a/46233446
	private <T> List<T> jsonArrayToObjectList(String json, Class<T> tClass) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, tClass);
		List<T> ts = mapper.readValue(json, listType);
		return ts;
	} // jsonArrayToObjectList

}
