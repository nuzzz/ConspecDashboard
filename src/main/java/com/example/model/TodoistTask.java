package com.example.model;

import java.util.Set;

public class TodoistTask extends TodoistObject{
	private long id;
	private boolean deleted;
	private String content;
	private long project_id;
	private int order; // childOrder
	private Set<Integer> label_list;
	private int priority;
	private Due due;
	private int indent;

	public TodoistTask(long id, boolean deleted, String content, long project_id, int order, Set<Integer> label_list, int priority, Due due,
			int indent) {
		super(id, deleted);
		this.content = content;
		this.project_id = project_id;
		this.order = order;
		this.label_list = label_list;
		this.priority = priority;
		this.due = due;
		this.indent = indent;
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Set<Integer> getLabel_list() {
		return label_list;
	}

	public void setLabel_list(Set<Integer> label_list) {
		this.label_list = label_list;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Due getDue() {
		return due;
	}

	public void setDue(Due due) {
		this.due = due;
	}

	public int getIndent() {
		return indent;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}

}
