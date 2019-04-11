package com.example.Model;

import com.fasterxml.jackson.annotation.JsonGetter;

//Dog breed model class
//{"breeds":
//	[{"weight":{"imperial":"40 - 65","metric":"18 - 29"},
//	  "height":{"imperial":"21 - 23","metric":"53 - 58"},
//	  "id":4,
//	  "name":"Airedale Terrier",
//	  "bred_for":"Badger, otter hunting",
//	  "breed_group":"Terrier",
//	  "life_span":"10 - 13 years",
//	  "temperament":"Outgoing, Friendly, Alert, Confident, Intelligent, Courageous",
//	  "origin":"United Kingdom, England"}],
public class Breed {
	private String weight;
	private String height;
	private int id;
	private String name;
	private String bred_for;
	private String breed_group;
	private String life_span;
	private String temperament;
	private String origin;
	
	public Breed(String weight, String height, int id, String name, String bred_for, String breed_group, String life_span, String temperment, String origin) {
		this.weight = weight;
		this.height = height;
		this.id = id;
		this.bred_for = bred_for;
		this.breed_group = breed_group;
		this.life_span = life_span;
		this.temperament = temperment;
		this.origin = origin;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}
	@JsonGetter
	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}
	@JsonGetter
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	@JsonGetter
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@JsonGetter
	public String getBred_for() {
		return bred_for;
	}

	public void setBred_for(String bred_for) {
		this.bred_for = bred_for;
	}
	@JsonGetter
	public String getBreed_group() {
		return breed_group;
	}

	public void setBreed_group(String breed_group) {
		this.breed_group = breed_group;
	}
	@JsonGetter
	public String getLife_span() {
		return life_span;
	}

	public void setLife_span(String life_span) {
		this.life_span = life_span;
	}
	@JsonGetter
	public String getTemperament() {
		return temperament;
	}

	public void setTemperament(String temperament) {
		this.temperament = temperament;
	}
	@JsonGetter
	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
}
