package com.example.helper;

import java.util.ArrayList;

import com.example.model.TodoistTempTask;

import net.sf.mpxj.Task;

public interface TodoistService {
	String createTodoistProject(String projectName);
		
	TodoistTempTask createTodoistTempTask(Task task, long project_id);

	void addTasksToTodoist(ArrayList<TodoistTempTask> addTheseTasks);
}