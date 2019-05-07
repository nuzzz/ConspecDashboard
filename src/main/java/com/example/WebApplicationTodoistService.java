package com.example;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.helper.TodoistService;
import com.example.model.Command;
import com.example.model.TodoistDue;
import com.example.model.TodoistTask;
import com.example.model.TodoistTempTask;

import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;

@Service
public class WebApplicationTodoistService implements TodoistService {
	String cid = "a0926c2f533a448bb14213e5bae8a964";
	String csecret = "3534a321bc474ce18ee8134aae2720f1";
	String ACCESS_TOKEN = "3a440b1a6f41b620823baa09a044c63771ff5809";

	// set on init
	private String client_id;
	private String client_secret;
	private int nonce;
	private String syncURL;

	private List<Command> commands = new ArrayList<Command>();
	// mapping of data_id from MSProject to temp_id generated by program
	Map<Integer, String> assignedTasks = new HashMap<Integer, String>();
	Map<Integer, Integer> assignedChildOrder = new HashMap<Integer, Integer>();
	
	List<TodoistTask> unaddedTasks = new ArrayList<TodoistTask>(); 

	// set after authorization
	private String accessToken;

	// .query("redirect_uri=http://localhost:5000/recieveOAuthReply")
	// https://todoist.com/oauth/authorize?client_id=0926c2f533a448bb14213e5bae8a964
	// https://www.getpostman.com/oauth2/callback
	// https://todoist.com/oauth/authorize?client_id=a0926c2f533a448bb14213e5bae8a964&scope=data:read
	// https://todoist.com/oauth/access_token?client_id=a0926c2f533a448bb14213e5bae8a964
	// a0926c2f533a448bb14213e5bae8a964
	// 3534a321bc474ce18ee8134aae2720f1

	public WebApplicationTodoistService() {
		init(ACCESS_TOKEN);
	}
	
	@Override
	public void init(String accessToken) {
		this.client_id = cid;
		this.client_secret = csecret;
		this.nonce = new Random(5).nextInt();
		this.accessToken = accessToken;
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("todoist.com")
				.path("/api/v8/sync");
		this.syncURL = uriBuilder.build().encode().toUriString();
	}


	@Override
	public String createTodoistProject(String projectName) {
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

		return projectTempID;
	}

	@Override
	public TodoistTempTask createTodoistTempTask(Task task, long project_id) {

		LocalDate startDate = task.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = task.getFinish().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String tempTaskID = assignedTasks.get(task.getID());

		TodoistDue due = new TodoistDue(endDate.format(dtf), "", "", "");
		int priority = 1;
		Set<Integer> labels = new HashSet<>();

		TodoistTempTask newTask = new TodoistTempTask(tempTaskID, task.getID(), task.getName(), project_id, startDate,
				endDate, due, priority, labels);
		newTask.setParent_id(assignedTasks.get(task.getParentTask().getID()));
		newTask.setChild_order(assignedChildOrder.get(task.getID()));

		return newTask;
	}
	
	public List<TodoistTempTask> createTempTaskList(ProjectFile projectData, long projectID) {
		List<TodoistTempTask> todoistTempTasks = new ArrayList<TodoistTempTask>();

		assignTempIdAndChildOrder(projectData);
		// Needs a mapping to determine parentid

		for (Task task : projectData.getTasks()) {
			// This task is considered if it does not contain the word Milestone
			// 1. If task name does not contain milestone
			// 2. If task is root task
			if (!task.getName().contains("Milestone") && task.getParentTask() != null) {
				todoistTempTasks.add(createTodoistTempTask(task, projectID));
			}
		}
		return todoistTempTasks;
	}
	
	public List<TodoistTempTask> tillDateFilter(List<TodoistTempTask> taskList, LocalDate date) {
		List<TodoistTempTask> filteredTasks = taskList.stream().filter(task -> !task.getStartDate().isAfter(date)).collect(Collectors.toList());
		return filteredTasks;
	}
	

	
	// Iterate thru all tasks assign do the following: 
	// 1. Create taskID to UUID mapping 
	// 2. Generate childOrder mapping using taskOrderParent hashmap to order tasks
	public void assignTempIdAndChildOrder(ProjectFile projectData) {
		Map<Integer, Integer> taskParentTracker = new HashMap<Integer, Integer>();

		for (Task currentTask : projectData.getTasks()) {
			assignedTasks.put(currentTask.getID(), UUID.randomUUID().toString());
			if (currentTask.getParentTask() != null) {
				Integer parentTaskID = currentTask.getParentTask().getID();
				if (!taskParentTracker.containsKey(parentTaskID)) {
					taskParentTracker.put(parentTaskID, 1);
				} else {
					taskParentTracker.put(parentTaskID, taskParentTracker.get(parentTaskID) + 1);
				}
				assignedChildOrder.put(currentTask.getID(), taskParentTracker.get(parentTaskID));
			}
		}
	}

//	public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
//		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//	}
//
//	public Date convertToDateViaInstant(LocalDate dateToConvert) {
//		return java.util.Date.from(dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
//	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public List<Command> getCommands() {
		return commands;
	}

	public void setCommands(List<Command> commands) {
		this.commands = commands;
	}

	public void clearCommands() {
		this.commands.clear();
	}


	public String getSyncURL() {
		return this.syncURL;
	}

}
