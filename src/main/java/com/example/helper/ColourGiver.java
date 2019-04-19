package com.example.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ColourGiver {
	private int colourIndex;
	private String[] colourArray;
	private Map<String, String> colourMap;
	
	public ColourGiver(String[] colourArray) throws Exception{
		if (colourArray.length==0){
			throw new Exception("Error: colour array cannot be empty");
		}
		resetColourIndex();
		//This is taken from outlook calendar category defaults
		this.colourArray = colourArray;
		this.colourMap = new HashMap<String, String>();
	}
	
	public ColourGiver(){
		resetColourIndex();
		//This is taken from outlook calendar category defaults
		this.colourArray = new String[]{"Purple category", "Blue category", 
										"Green category", "Yellow category", 
										"Orange category", "Red category"};
		this.colourMap = new HashMap<String, String>();
	}
	
	public void colourIndexPlusOne(){
		this.colourIndex = this.colourIndex + 1;
		if(this.colourIndex+1 > this.colourArray.length){
			resetColourIndex();
		}	
	}
	
	public int getColourIndex(){
		return this.colourIndex;
	}
	
	public String[] getColourArray(){
		return this.colourArray;
	}
	
	public HashMap<String, String> getColourMap(){
		return (HashMap<String, String>) this.colourMap;
	}
	
	private void resetColourIndex(){
		this.colourIndex = 0;
	}
	
	private String getColour(){
		return this.colourArray[this.colourIndex];
	}

	public String getColourCategory(String category){
		String colour = "";
		//if colour not in mapping, add it to mapping.
		if(!this.colourMap.containsKey(category)){
			colour = getColour();
			this.colourMap.put(category,colour);
			colourIndexPlusOne();
		//if colour in mapping retrieve it from map
		}else{
			colour = colourMap.get(category);
		}
		return colour;
	}	
}
