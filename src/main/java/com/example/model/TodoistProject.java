package com.example.model;

public class TodoistProject extends TodoistObject{
	private long id;
	private boolean deleted;
	private String name;
	private long parent_id;
	private int childOrder;
	private int indent;
	
	public TodoistProject(long id, boolean deleted, String name, long parent_id, int childOrder, int indent) {
		super(id, deleted);
		this.name = name;
		this.parent_id = parent_id;
		this.childOrder = childOrder;
		this.indent = indent;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getParent_id() {
		return parent_id;
	}
	public void setParent_id(long parent_id) {
		this.parent_id = parent_id;
	}
	public int getChildOrder() {
		return childOrder;
	}
	public void setChildOrder(int childOrder) {
		this.childOrder = childOrder;
	}
	public int getIndent() {
		return indent;
	}
	public void setIndent(int indent) {
		this.indent = indent;
	}
	
	
}
