package com.example.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class TodoistTempTask {
	//task identifiers
	private String temp_id;
	
	//task data
	private int data_id;
	private String content; //	The text of the task.
	private long project_id; //	The id of the project to add the task to (a number or a temp id). By default the task is added to the user’s Inbox project.
	
	private LocalDate startDate;
	private LocalDate endDate;
	
	private TodoistDue due; 
	
	private int priority; //	The priority of the task (a number between 1 and 4, 4 for very urgent and 1 for natural).
	private String parent_id; //The id of the parent task. Set to null for root tasks
	private int child_order; //The order of task. Defines the position of the task among all the tasks with the same parent_id

	//unused
	private int day_order; //	The order of the task inside the Today or Next 7 days view (a number, where the smallest value would place the task at the top).
	private int collapsed; //Whether the task’s sub-tasks are collapsed (where 1 is true and 0 is false).
	private Set<Integer> labels; //The tasks labels (a list of label ids such as [2324,2525]).
	private int assigned_by_uid; //The id of user who assigns the current task. This makes sense for shared projects only. Accepts 0 or any user id from the list of project collaborators. If this value is unset or invalid, it will be automatically setup to your uid.
	private int responsible_uid; //The id of user who is responsible for accomplishing the current task. This makes sense for shared projects only. Accepts any user id from the list of project collaborators or null or an empty string to unset.
	private boolean auto_reminder; // When this option is enabled, the default reminder will be added to the new item if it has a due date with time set. See also the auto_reminder user option for more info about the default reminder.
	private boolean auto_parse_labels; //When this option is enabled, the labels will be parsed from the task content and added to the task. In case the label doesn’t exist, a new one will be created. 
	
	public TodoistTempTask(String temp_id, Integer data_id, String content, long project_id,
						LocalDate startDate, LocalDate endDate, TodoistDue due, 
						int priority, Set<Integer> labels) {
		this.temp_id = temp_id;
		this.data_id = data_id;
		this.content = content;
		this.project_id = project_id;
		
		this.startDate = startDate;
		this.endDate = endDate;
		this.due = due;
		
		this.priority = priority;
		this.labels = labels;
	}

	public String getTemp_id() {
		return temp_id;
	}

	public void setTemp_id(String temp_id) {
		this.temp_id = temp_id;
	}

	public int getData_id() {
		return data_id;
	}

	public void setData_id(int data_id) {
		this.data_id = data_id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getProject_id() {
		return project_id;
	}

	public void setProject_id(long project_id) {
		this.project_id = project_id;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public TodoistDue getDue() {
		return due;
	}

	public void setDue(TodoistDue due) {
		this.due = due;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public int getChild_order() {
		return child_order;
	}

	public void setChild_order(int child_order) {
		this.child_order = child_order;
	}

	public Set<Integer> getLabels() {
		return labels;
	}

	public void setLabels(Set<Integer> labels) {
		this.labels = labels;
	}
}
