package com.example;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.helper.TodoistService;

@Service
public class WebApplicationTodoistService implements TodoistService {
	String cid =  "a0926c2f533a448bb14213e5bae8a964";
	String csecret="3534a321bc474ce18ee8134aae2720f1";

	//set on init
	private String client_id;
	private String client_secret;
	private int nonce;
	
	//set after authorization
	private String access_token;
			
	//.query("redirect_uri=http://localhost:5000/recieveOAuthReply")
	//https://todoist.com/oauth/authorize?client_id=0926c2f533a448bb14213e5bae8a964
	//https://www.getpostman.com/oauth2/callback
	//https://todoist.com/oauth/authorize?client_id=a0926c2f533a448bb14213e5bae8a964&scope=data:read
	//https://todoist.com/oauth/access_token?client_id=a0926c2f533a448bb14213e5bae8a964
	//a0926c2f533a448bb14213e5bae8a964
	//3534a321bc474ce18ee8134aae2720f1
	
	public WebApplicationTodoistService() {
		
	}
	
	@Override
	public void init() {
		this.client_id = cid;
		this.client_secret = csecret;
		this.nonce = new Random(5).nextInt();
	}

	@Override
	public void addTask() {

		
	}

	public void setAccessToken(String access_token) {
		this.access_token = access_token;
	}
	
	
	public String getAccessToken() {
		return this.access_token;
	}

}
