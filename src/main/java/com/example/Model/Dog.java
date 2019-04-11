package com.example.Model;

import com.fasterxml.jackson.annotation.JsonGetter;

//[{"breeds":
//	[{"weight":{"imperial":"40 - 65","metric":"18 - 29"},
//	  "height":{"imperial":"21 - 23","metric":"53 - 58"},
//	  "id":4,
//	  "name":"Airedale Terrier",
//	  "bred_for":"Badger, otter hunting",
//	  "breed_group":"Terrier",
//	  "life_span":"10 - 13 years",
//	  "temperament":"Outgoing, Friendly, Alert, Confident, Intelligent, Courageous",
//	  "origin":"United Kingdom, England"}],
//	"id":"SJZIJgqEX",
//	"url":"https://cdn2.thedogapi.com/images/SJZIJgqEX_1280.jpg",
//	"width":333,
//	"height":500}]
public class Dog {
	private Breed breed;
	private String id;
	private String url;
	private int width;
	private int height;
	
	
	public Dog(Breed breed, String id, String url, int width, int height) {
		super();
		this.breed = breed;
		this.id = id;
		this.url = url;
		this.width = width;
		this.height = height;
	}

	@JsonGetter
	public Breed getBreed() {
		return breed;
	}


	public void setBreed(Breed breed) {
		this.breed = breed;
	}

	@JsonGetter
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	@JsonGetter
	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}

	@JsonGetter
	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}

	@JsonGetter
	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}
	
	

}
