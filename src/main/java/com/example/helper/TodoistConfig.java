package com.example.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TodoistConfig {
	@Value("#{environment.TODOIST_CLIENT_ID}")
	private String clientID;
	
	@Value("#{environment.TODOIST_CLIENT_SECRET}")
	private String clientSecret;

	@Value("#{environment.TODOIST_ACCESS_TOKEN}")
	private String accessToken;
	
	@Bean(name = "clientID")
	public String getClientID() {
		return clientID;
	}
	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	
	@Bean(name = "clientSecret")
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	@Bean(name = "accessToken")
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}