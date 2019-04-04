package com.example;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ColourGiverTest {
	
	@Test	
	public void initColourIndexZero(){
		ColourGiver colourGiver = new ColourGiver();
		assertEquals(0, colourGiver.getColourIndex());
	}
	
	@Test	
	public void initColourArray(){
		String[] expectedArray =  {"Purple category", "Blue category", 
				"Green category", "Yellow category", 
				"Orange category", "Red category"};
		ColourGiver colourGiver = new ColourGiver();
		Assertions.
			
	}
	
}
