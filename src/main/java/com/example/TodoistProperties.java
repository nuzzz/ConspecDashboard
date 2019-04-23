package com.example;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("todoist")
public class TodoistProperties {
	
	private String clientID;
	private String clientSecret;
	
	public String getClientID() {
		return clientID;
	}
	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}