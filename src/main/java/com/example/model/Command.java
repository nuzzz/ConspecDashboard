package com.example.model;

import java.util.Map;
import java.util.Set;

public class Command {
	private String type;
	private String uuid;
	private String temp_id;
	private Map<String, Object> args;
	
	public Command() {
		
	}
	
	public Command(String type, String uuid, String temp_id, Map<String, Object> args) {
		this.type = type;
		this.uuid = uuid;
		this.temp_id = temp_id;
		this.args = args;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getTemp_id() {
		return temp_id;
	}
	public void setTemp_id(String temp_id) {
		this.temp_id = temp_id;
	}
	public Map<String, Object> getArgs() {
		return args;
	}
	public void setArgs(Map<String, Object	> args) {
		this.args = args;
	}
	
	public String toString() {
		Set<String> keys = args.keySet();
		String arguments = "";
		for (String key: keys) {
			arguments += key + " " + args.get(key); 
		}
		
		return "type: " + type + " uuid: " + uuid + " temp_id: " + temp_id + " arguments: "+args; 
	}
	
}
