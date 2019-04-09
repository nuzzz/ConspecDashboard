package com.example;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class WeeklySummaryHelper {
	ArrayList<Integer> dayDateList; 
	public WeeklySummaryHelper( ){
		this.dayDateList = new ArrayList<Integer>();
	}

	public ArrayList<Integer> getDayDateList() {
		return dayDateList;
	}

	public void setDayDateList(ArrayList<Integer> dayDateList) {
		this.dayDateList = dayDateList;
	}
	
	//Gives all the dates in a range that are xxx-day (Day Of Week)
	public void setDateListOfDay(DayOfWeek dayOfWeek, Date startDate, Date endDate) throws IllegalArgumentException{
		LocalDate start = convertToLocalDateViaInstant(startDate);
		LocalDate end = convertToLocalDateViaInstant(endDate);
		
		if(start.equals(end)) {
			//add if it the startdate is the day of week
			addDayDateListIfDay(dayOfWeek, start);
			return;
		}
		
		if(start.isBefore(end)) {
			addDayDateListInRangeIfDay(dayOfWeek, start, end);
		} else {
			throw new IllegalArgumentException("WeeklySummaryHelper Error: Start is before end, fail to set dayDateList");
		}
	}
	
	public void addDayDateListInRangeIfDay(DayOfWeek dayOfWeek, LocalDate startDate, LocalDate endDate) {
		LocalDate iteratorDate = LocalDate.of(startDate.getYear(), startDate.getMonthValue(), startDate.getDayOfMonth());
		
		//while(iteratorDate.isBefore(endDate) || iteratorDate.isEqual(endDate)) {
		while(!iteratorDate.isAfter(endDate)) {
			if(isXDay(dayOfWeek, iteratorDate)){
				addDayDateListIfDay(dayOfWeek, iteratorDate);
				iteratorDate = iteratorDate.plusDays(7);
			}else{
				Integer dayDateInteger = convertDateTo8Integer(iteratorDate.with(dayOfWeek));
				if(dayDateInteger!=-1 && !this.dayDateList.contains(dayDateInteger)){
					this.dayDateList.add(dayDateInteger);
				}
				iteratorDate = iteratorDate.plusDays(1);
			}
		}
	}
	
	public void addDayDateListIfDay(DayOfWeek dayOfWeek, LocalDate localDate) {
		
		if(isXDay(dayOfWeek, localDate)){
			//if doesnt contain the date, add it
			Integer dayDateInteger = convertDateTo8Integer(localDate);
			if(dayDateInteger!=-1 && !this.dayDateList.contains(dayDateInteger)){
				this.dayDateList.add(dayDateInteger);
			}
		}else{
			Integer dayDateInteger = convertDateTo8Integer(localDate.with(dayOfWeek));
			if(dayDateInteger!=-1 && !this.dayDateList.contains(dayDateInteger)){
				this.dayDateList.add(dayDateInteger);
			}
		}
	}
	
	public boolean isXDay(DayOfWeek day,LocalDate startDate){
		
		if (startDate.getDayOfWeek().equals(day)){
			return true;
		}
		return false;
	}
	

	public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
	    return dateToConvert.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDate();
	}
	public Date convertToDateViaInstant(LocalDate dateToConvert) {
	    return java.util.Date.from(dateToConvert.atStartOfDay()
	      .atZone(ZoneId.systemDefault())
	      .toInstant());
	}
	
	public Integer convertDateTo8Integer(LocalDate date) {
		Integer dateInt = new Integer(-1);
		String dateIntString = String.format("%04d", date.getYear()) + String.format("%02d",date.getMonthValue()) + String.format("%02d",date.getDayOfMonth());
		try{
			dateInt = Integer.parseInt(dateIntString);
		}catch (NumberFormatException nfe){
			System.out.println("convertDateTo8Int Error: Failed to convert to date integer\n"+ nfe);
			return dateInt;
		}	
		return dateInt;
	}
}
