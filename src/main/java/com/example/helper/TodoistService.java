package com.example.helper;

import com.example.model.TodoistTask;

import net.sf.mpxj.Task;

public interface TodoistService {

    void init(String accessToken);

    void addTask();
    
	void createTodoistProject(String projectName);

	TodoistTask createTodoistTask(Task task, long project_id, int taskNumber, int indent);
}