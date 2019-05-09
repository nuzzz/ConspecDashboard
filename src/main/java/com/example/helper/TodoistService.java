package com.example.helper;

import java.util.ArrayList;

import com.example.model.TodoistTask;
import com.example.model.TodoistTempTask;

import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;

public interface TodoistService {

    void init(String accessToken);

	String createTodoistProject(String projectName);
		
	TodoistTempTask createTodoistTempTask(Task task, long project_id);

	void addTasksToTodoist(ArrayList<TodoistTempTask> addTheseTasks);
}