package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ColourGiver {
	private int colourIndex;
	private String[] colourArray;
	private Map<String, String> colourMap;
	
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
		return this.getColourMap();
	}
	
	private void resetColourIndex(){
		this.colourIndex = 0;
	}
	
	private String getNextColour(){
		colourIndexPlusOne();
		return this.colourArray[this.colourIndex];
	}
	
	private String getColour(){
		return this.colourArray[this.colourIndex];
	}

	public String getColourCategory(String category){
		String colour = "";
		if(this.colourMap.containsKey(category)){
			//get the category colour for the value
			colour = colourMap.entrySet().stream()
					  .filter(e -> e.getValue().equals(category))
					  .map(Map.Entry::getKey)
					  .findFirst()
					  .orElse(null);
			
		}else{
			addCategory(category);
			colour = this.colourMap.get(category);
		}
		return colour;
	}

	private void addCategory(String category){
		//if colourMap does not contain category, add it
		if(this.colourMap.get(category) == null){
			this.colourMap.put(category,getColour());
		}else{
			this.colourMap.put(category,getNextColour());
		}
	}
	
	
}
