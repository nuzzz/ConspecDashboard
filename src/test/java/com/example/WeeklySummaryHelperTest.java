package com.example;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.helper.WeeklySummaryHelper;


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
	public void getDateListOfDayStartAndEndSameReturnsOne(){
		// the date 8/4/2019 is a monday
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		Assertions.assertEquals(1, wsh.getDayDateList().size());
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
		Assertions.assertEquals(1, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayEndPlusTwo() {
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(2));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		Assertions.assertEquals(1, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayEndPlusThree() {
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(3));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		Assertions.assertEquals(1, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayEndPlusSevenReturnsTwo() {
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(7));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		Assertions.assertEquals(2, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDayFixedDateReturnsCorrectly() {
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,12,17));
		DayOfWeek day = DayOfWeek.MONDAY;
		wsh.setDateListOfDay(day, startDate, endDate);
		Assertions.assertEquals(37, wsh.getDayDateList().size());
	}
	
	@Test
	public void getDateListOfDaySameDayRangeReturnsPreviousMonday1() {
		//Start and end date set to TUESDAY 16/12/2019
		startDate = wsh.convertToDateViaInstant(LocalDate.of(2019,12,16).plusDays(1));
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,12,16).plusDays(1));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		LocalDate start = wsh.convertToLocalDateViaInstant(startDate);
		Integer expected = wsh.convertDateTo8Integer(start.minusDays(1));

		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(1,wsh.getDayDateList().size());
		Assertions.assertEquals(expected,wsh.getDayDateList().get(0));
	}
	
	@Test
	public void getDateListOfDaySameDayRangeReturnsPreviousMonday2() {
		//Start and end date set to TUESDAY 16/12/2019
		startDate = wsh.convertToDateViaInstant(LocalDate.of(2019,12,16).plusDays(2));
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,12,16).plusDays(2));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		LocalDate start = wsh.convertToLocalDateViaInstant(startDate);
		Integer expected = wsh.convertDateTo8Integer(start.minusDays(2));

		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(1,wsh.getDayDateList().size());
		Assertions.assertEquals(expected,wsh.getDayDateList().get(0));
	}
	
	@Test
	public void getDateListOfDaySameDayRangeReturnsPreviousMonday3() {
		//Start and end date set to TUESDAY 16/12/2019
		startDate = wsh.convertToDateViaInstant(LocalDate.of(2019,12,16).plusDays(3));
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,12,16).plusDays(3));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		LocalDate start = wsh.convertToLocalDateViaInstant(startDate);
		Integer expected = wsh.convertDateTo8Integer(start.minusDays(3));

		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(1,wsh.getDayDateList().size());
		Assertions.assertEquals(expected,wsh.getDayDateList().get(0));
	}
	
	@Test
	public void getDateListOfDayRangeNotContainDayReturnsPreviousMonday1() {
		//put start to tuesday
		LocalDate startPlusOneDay = wsh.convertToLocalDateViaInstant(startDate).plusDays(1);
		startDate = wsh.convertToDateViaInstant(startPlusOneDay);
		//put end to thursday
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(3));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		Integer expected = wsh.convertDateTo8Integer(startPlusOneDay.minusDays(1));

		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(1,wsh.getDayDateList().size());
		Assertions.assertEquals(expected,wsh.getDayDateList().get(0));
	}
	
	@Test
	public void getDateListOfDayRangeNotContainDayReturnsPreviousMonday2() {
		//put start to wednesday
		LocalDate startPlusTwoDay = wsh.convertToLocalDateViaInstant(startDate).plusDays(2);
		startDate = wsh.convertToDateViaInstant(startPlusTwoDay);
		//put end to friday
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(4));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		Integer expected = wsh.convertDateTo8Integer(startPlusTwoDay.minusDays(2));

		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(1,wsh.getDayDateList().size());
		Assertions.assertEquals(expected,wsh.getDayDateList().get(0));
	}
	
	@Test
	public void getDateListOfDayRangeNotContainDayReturnsPreviousMonday3() {
		//put start to friday
		LocalDate startPlusFourDay = wsh.convertToLocalDateViaInstant(startDate).plusDays(4);
		startDate = wsh.convertToDateViaInstant(startPlusFourDay);
		//put end to sunday
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(6));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		Integer expected = wsh.convertDateTo8Integer(startPlusFourDay.minusDays(4));

		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(1,wsh.getDayDateList().size());
		Assertions.assertEquals(expected,wsh.getDayDateList().get(0));
	}
	
	@Test
	public void getDateListOfDayRangeNotContainDayReturnsPreviousMondayAndNextMonday1() {
		//put start to friday
		LocalDate startPlusFourDay = wsh.convertToLocalDateViaInstant(startDate).plusDays(4);
		startDate = wsh.convertToDateViaInstant(startPlusFourDay);
		//put end as following week thursday 7+3
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(10));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		Integer expectedFirst = wsh.convertDateTo8Integer(startPlusFourDay.minusDays(4));
		Integer expectedSecond = wsh.convertDateTo8Integer(startPlusFourDay.plusDays(3));

		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(2,wsh.getDayDateList().size());
		
		Assertions.assertEquals(new Integer(expectedFirst), wsh.getDayDateList().get(0));
		Assertions.assertEquals(new Integer(expectedSecond), wsh.getDayDateList().get(1));
	}
	
	@Test
	public void getDateListOfDayRangeNotContainDayReturnsPreviousMondayAndNextMonday2() {
		//put start to wednesday
		LocalDate startPlusTwoDay = wsh.convertToLocalDateViaInstant(startDate).plusDays(2);
		startDate = wsh.convertToDateViaInstant(startPlusTwoDay);
		//put end to following wednesday
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,8).plusDays(9));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		Integer expectedFirst = wsh.convertDateTo8Integer(startPlusTwoDay.minusDays(2));
		Integer expectedSecond = wsh.convertDateTo8Integer(startPlusTwoDay.plusDays(5));

		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(2,wsh.getDayDateList().size());
		Assertions.assertEquals(expectedFirst,wsh.getDayDateList().get(0));
		Assertions.assertEquals(expectedSecond,wsh.getDayDateList().get(1));
	}
	
	@Test
	public void getDateListOfDayRangeNotContainDayReturnsPreviousMondayAndNextMonday3() {
		//put start as saturday 06/04/2019
		LocalDate startLocalDate = LocalDate.of(2019, 4, 6);
		startDate = wsh.convertToDateViaInstant(startLocalDate);
		//put end as tuesday 30/04/2019
		Date endDate = wsh.convertToDateViaInstant(LocalDate.of(2019,4,30));
		
		DayOfWeek day = DayOfWeek.MONDAY;
		
		Integer expectedFirst = wsh.convertDateTo8Integer(startLocalDate.minusDays(5));
		Integer expectedSecond = wsh.convertDateTo8Integer(startLocalDate.plusDays(2));
		Integer expectedThird = wsh.convertDateTo8Integer(startLocalDate.plusDays(9));
		Integer expectedForth = wsh.convertDateTo8Integer(startLocalDate.plusDays(16));
		Integer expectedFifth = wsh.convertDateTo8Integer(startLocalDate.plusDays(23));
		
		wsh.setDateListOfDay(day, startDate, endDate);
		
		Assertions.assertEquals(5,wsh.getDayDateList().size());
		Assertions.assertEquals(expectedFirst,wsh.getDayDateList().get(0));
		Assertions.assertEquals(expectedSecond,wsh.getDayDateList().get(1));
		Assertions.assertEquals(expectedThird,wsh.getDayDateList().get(2));
		Assertions.assertEquals(expectedForth,wsh.getDayDateList().get(3));
		Assertions.assertEquals(expectedFifth,wsh.getDayDateList().get(4));

	}
}
