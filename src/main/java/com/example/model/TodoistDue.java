package com.example.model;	

public class TodoistDue {
	private String date;
	private String datetime;
	private String string;
	private String timezone;
	public TodoistDue(String date, String datetime, String string, String timezone) {
		this.date = date;
		this.datetime = datetime;
		this.string = string;
		this.timezone = timezone;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public String getString() {
		return string;
	}
	public void setString(String string) {
		this.string = string;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
}
