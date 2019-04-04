package com.example;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
	
@DisplayName("ColourGiver class tester")
public class ColourGiverTest {
	
	@Test	
	public void initColourIndexZero(){
		ColourGiver colourGiver = new ColourGiver();
		assertEquals(0, colourGiver.getColourIndex());
	}
	
	@Test	
	public void initColourArray(){
		String[] expectedOutput =  {"Purple category", "Blue category", 
				"Green category", "Yellow category", 
				"Orange category", "Red category"};
		ColourGiver colourGiver = new ColourGiver();
		Assertions.assertArrayEquals(expectedOutput,colourGiver.getColourArray());
	}
	
	@Test	
	public void initWithEmptyArrayThrowsException(){
		Assertions.assertThrows(Exception.class, ()->{
			String[] customArray = {};
			ColourGiver colourGiver = new ColourGiver(customArray);
		});
	}
	
	@Test	
	public void initWithNonEmptyArray(){
		try{
			String[] customArray = {"Red category", "Blue category", "Green category"};
			ColourGiver colourGiver = new ColourGiver(customArray);
			Assertions.assertArrayEquals(customArray, colourGiver.getColourArray());
		}catch (Exception e){
			Assertions.fail("initWithNonEmptyArray: Exception should not be thrown");
		}
		
	}
	
	@Test
	public void initColourMap(){
		ColourGiver colourGiver = new ColourGiver();
		HashMap<String, String> expectedOutput = new HashMap<String,String>();
		
	}
	
	@Test
	public void getColourCategoryInitPurple(){
		ColourGiver colourGiver = new ColourGiver();
		String expectedCategory = "Purple category";
		String actualCategory = colourGiver.getColourCategory("abcd");
		assertEquals(expectedCategory, actualCategory);
	}
	
	@Test
	public void getColourCategoryAfterInitBlue(){
		ColourGiver colourGiver = new ColourGiver();
		String expectedCategory = "Blue category";
		
		String colour1 = colourGiver.getColourCategory("abcd");
		String colour2 = colourGiver.getColourCategory("bcde");	
		assertEquals(expectedCategory, colour2);
	}
	
	@Test
	public void getColourCategoryRepeat(){
		ColourGiver colourGiver = new ColourGiver();
		String expectedCategory = "Purple category";

		String colour1 = colourGiver.getColourCategory("abcd");
		String colour2 = colourGiver.getColourCategory("bcde");
		String colour3 = colourGiver.getColourCategory("abcd");
		System.out.println(colour1);
		System.out.println(colour2);
		System.out.println(colour3);
		assertEquals(expectedCategory, colour3);
	}
	
	@Test
	public void getColourCategoryFullLoop(){
		ColourGiver colourGiver = new ColourGiver();
		String[] expectedArray =  {"Purple category", "Blue category", 
				"Green category", "Yellow category",
				"Orange category", "Red category"};
		
		String colour1 = colourGiver.getColourCategory("111");
		String colour2 = colourGiver.getColourCategory("222");
		String colour3 = colourGiver.getColourCategory("333");
		String colour4 = colourGiver.getColourCategory("444");
		String colour5 = colourGiver.getColourCategory("555");
		String colour6 = colourGiver.getColourCategory("666");
		String colour7 = colourGiver.getColourCategory("777");
		String colour8 = colourGiver.getColourCategory("888");
		String colour9 = colourGiver.getColourCategory("999");
		
		assertEquals(expectedArray[0], colour1);
		assertEquals(expectedArray[1], colour2);
		assertEquals(expectedArray[2], colour3);
		assertEquals(expectedArray[3], colour4);
		assertEquals(expectedArray[4], colour5);
		assertEquals(expectedArray[5], colour6);
		assertEquals(expectedArray[0], colour7);
		assertEquals(expectedArray[1], colour8);
		assertEquals(expectedArray[2], colour9);
	}
}
