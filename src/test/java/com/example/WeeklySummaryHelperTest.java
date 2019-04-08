package com.example;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("WeeklySummaryHelper class tester")
public class WeeklySummaryHelperTest {
	
	private WeeklySummaryHelper wsh;
	private Date startDate;
	
	@BeforeEach
	void setUp() throws Exception {
		this.wsh = new WeeklySummaryHelper();
		//the date 8/4/2019 is a monday
		this.startDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8));
		
	}

	
	@Test
	public void getDateListOfDayStartAndEndSameReturnsZero(){
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8));
		DayOfWeek day = DayOfWeek.TUESDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		ArrayList<Integer> listOfMondays = wsh.getDayDateList();
		
		Assertions.assertEquals(0, listOfMondays.size());
	}
	
	@Test
	public void getDateListOfDayStartAndEndSameReturnsOne(){
		// the date 8/4/2019 is a monday
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		ArrayList<Integer> listOfMondays = wsh.getDayDateList();
		
		Assertions.assertEquals(1, listOfMondays.size());
	}
	
	@Test
	public void getDateListOfDayEndBeforeStartThrowsException() {
		// the date 8/4/2019 is a monday
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).minusDays(1));
		DayOfWeek day = DayOfWeek.MONDAY;
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			wsh.setDateListOfDay(day, startDate, endDate);
		});
		
		Assertions.assertEquals(0, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayEndPlusOne() {
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(1));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		ArrayList<Integer> listOfMondays = wsh.getDayDateList();
		
		Assertions.assertEquals(1, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayEndPlusTwo() {
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(2));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		ArrayList<Integer> listOfMondays = wsh.getDayDateList();
		
		Assertions.assertEquals(1, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayEndPlusThree() {
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(3));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		ArrayList<Integer> listOfMondays = wsh.getDayDateList();
		Assertions.assertEquals(1, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayEndPlusSevenReturnsTwo() {
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(7));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		ArrayList<Integer> listOfMondays = wsh.getDayDateList();
		
		Assertions.assertEquals(2, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayFixedDateReturnsCorrectly() {
		LocalDate start = wsh.convertToLocalDateViaInstant(startDate);
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,12,17));
		LocalDate end = wsh.convertToLocalDateViaInstant(endDate);
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		ArrayList<Integer> listOfMondays = wsh.getDayDateList();
		
		Assertions.assertEquals(37, wsh.getDayDateList().size());
		
		
	}
	
	
}
