package com.example;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.helper.TodoistService;
import com.example.model.Command;
import com.example.model.TodoistDue;
import com.example.model.TodoistTask;
import com.example.model.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.mpxj.Task;

@Service
public class WebApplicationTodoistService implements TodoistService {
	String cid =  "a0926c2f533a448bb14213e5bae8a964";
	String csecret="3534a321bc474ce18ee8134aae2720f1";
	String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";
	
	//set on init
	private String client_id;
	private String client_secret;
	private int nonce;
	private String syncURL;
	
	private List<Command> commands = new ArrayList<Command>();
	
	//set after authorization
	private String accessToken;
			
	//.query("redirect_uri=http://localhost:5000/recieveOAuthReply")
	//https://todoist.com/oauth/authorize?client_id=0926c2f533a448bb14213e5bae8a964
	//https://www.getpostman.com/oauth2/callback
	//https://todoist.com/oauth/authorize?client_id=a0926c2f533a448bb14213e5bae8a964&scope=data:read
	//https://todoist.com/oauth/access_token?client_id=a0926c2f533a448bb14213e5bae8a964
	//a0926c2f533a448bb14213e5bae8a964
	//3534a321bc474ce18ee8134aae2720f1
	
	public WebApplicationTodoistService() {
		init(ACCESS_TOKEN);
	}
	
	public List<Command> getCommands() {
		return commands;
	}

	public void setCommands(List<Command> commands) {
		this.commands = commands;
	}
	
	public void clearCommands(){
		this.commands.clear();
	}

	@Override
	public void init(String accessToken) {
		this.client_id = cid;
		this.client_secret = csecret;
		this.nonce = new Random(5).nextInt();
		this.accessToken = accessToken;
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host("todoist.com")
				.path("/api/v8/sync");
		this.syncURL = uriBuilder.build().encode().toUriString();
	}
	
	public String getSyncURL() {
		return this.syncURL;
	}

	@Override
	public void addTask() {

		
	}
	
	@Override
	public void createTodoistProject(String projectName) {
		this.getAccessToken();
		String projectTempID = UUID.randomUUID().toString();
		String commandID = UUID.randomUUID().toString();
		
		// ACCESS_TOKEN = this.accessToken
		// https://todoist.com/api/v8/sync ==> this.syncURL
		
		Map<String, Object> projectArguments = new HashMap<String, Object>();
		projectArguments.put("name", projectName);
//		projectArguments.put("color", "");
//		projectArguments.put("parent_id", "");
//		projectArguments.put("child_order", "");
//		projectArguments.put("is_favorite", "");
		
		Command addProjectCommand = new Command("project_add", commandID, projectTempID, projectArguments);
		
		this.commands = new ArrayList<Command>();
		this.commands.add(addProjectCommand);
	}
	
	@Override
	public TodoistTask createTodoistTask(Task task, long project_id, int taskNumber, int indent) {
		long taskID = -1L;
		String tempTaskID = UUID.randomUUID().toString(); 
		Set<Integer> newLabelList = new HashSet<Integer>();
		//public TodoistTask(long id, String tempID, boolean deleted, String content, long project_id, 
		//					int order, Set<Integer> label_list, int priority, TodoistDue due, int indent) {
		//tempid
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd"); 
		TodoistDue due = new TodoistDue(task.getFinish().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(dtf),"","","");
		
		TodoistTask newTask = new TodoistTask(taskID,false,tempTaskID,task.getName(),project_id, taskNumber, newLabelList, 1, due, indent);
		
		return null;
	}

	
//	
//	Token access_token = new Token(ACCESS_TOKEN);
//	UUID tempID = UUID.randomUUID();
//	UUID cmdID = UUID.randomUUID();
//	ResponseEntity<String> result = new ResponseEntity<String>(HttpStatus.NOT_FOUND);
//	// https://todoist.com/api/v8/sync
//	UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("todoist.com")
//			.path("/api/v8/sync");
//	String syncURL = uriBuilder.build().encode().toUriString();		
//	
//	UUID tempID2 = UUID.randomUUID();
//	UUID cmdID2 = UUID.randomUUID();
//	//String date, String timezone, String string, String lang, boolean isRecurring
//	TodoistDue d = new TodoistDue("2019-04-27", "", "", "");
//			
//	Map<String, Object> arguments2 = new HashMap<String, Object>();
//	arguments2.put("content", "sub task 4");
//	arguments2.put("due", d);//.replace("\\\\", ""));
//	arguments2.put("indent", "2");
//	arguments2.put("project_id", String.valueOf(2209532923L));
//	
//	
//	Command addItemCommand = new Command("item_add", cmdID2.toString(), tempID2.toString(), arguments2);
//	
//	List<Command> commands = new ArrayList<Command>();
//	//commands.add(addProjCommand);
//	commands.add(addItemCommand);
//			
//
//	HttpHeaders headers = new HttpHeaders();
//	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//	headers.set("token", access_token.getToken());
//	headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//	headers.set("Authorization", "Bearer " + ACCESS_TOKEN);
//
//	try {
//		ObjectMapper objectMapper = new ObjectMapper();
//		String jsonStr = objectMapper.writeValueAsString(commands);
//		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
//		map.add("commands", jsonStr);
//		
//		MultiValueMap<String, Object> commandsMap = new LinkedMultiValueMap<>();
//		commandsMap.add("commands", jsonStr);
//		
//	    HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(commandsMap, headers);
//		result = restTemplate.postForEntity(syncURL, entity, String.class);
//		System.out.println("Entity: " + entity.getBody());
//		System.out.println("___________________");
//
//		System.out.println("Result: " + result);
//		System.out.println("___________________");
//	}
//
//	catch (JsonProcessingException e) {
//		e.printStackTrace();
//	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	
	public String getAccessToken() {
		return this.accessToken;
	}
}
