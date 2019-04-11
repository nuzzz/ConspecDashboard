package com.example;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Summary;
import biweekly.util.Duration;


import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.reader.UniversalProjectReader;

public class ConspecProjectManager {
	private String projectFilename;
	private ProjectFile projectData;
	private String[] colourArray =  {"Purple category", "Blue category", 
								"Green category", "Yellow category", 
								"Orange category", "Red category"};
	
	ConspecProjectManager(String projectFilename) throws MPXJException{
		ProjectReader reader = new UniversalProjectReader();
		try {
			this.projectData = reader.read(projectFilename);
		} catch (MPXJException e) {
			System.out.println("Failed to read project file: " + e);
		}
	}
	
	public ProjectFile getProjectFile(){
		return this.projectData;
	}
	
	public ArrayList<ICalendar> createProjectCalendars(ProjectFile project, String fileType){
		ICalendar overviewCal = createOverviewCalendar(project, fileType);
		ICalendar reminderCal = createReminderCalendar(project, fileType);
		ICalendar weeklySummaryCal = createWeeklySummaryCalendar(project, fileType);
		
		ArrayList<ICalendar> calendars = new ArrayList<ICalendar>();
		
		calendars.add(overviewCal);
		calendars.add(reminderCal);
		calendars.add(weeklySummaryCal);
		
		
		return calendars;
	}
	
	public ICalendar createOverviewCalendar(ProjectFile project, String fileType){
		ICalendar overviewCal = new ICalendar();
		switch(fileType){
			case("ICS"):
				overviewCal = createOverviewCalendarICS(project);
				break;
			case("CSV"):
				//TODO: do nothing for now
				break;	
			default:
				//invalid file type
				break;
		}
		return overviewCal;
	}
	

	public ICalendar createReminderCalendar(ProjectFile project, String fileType){
		ICalendar reminderCal = new ICalendar();
		switch(fileType){
			case("ICS"):
				reminderCal = createReminderCalendarICS(project);
				break;
			case("CSV"):
				//TODO: do nothing for now
				break;
			default:
				break;
		}
		return reminderCal;
	}
	
	public ICalendar createWeeklySummaryCalendar(ProjectFile project, String fileType){
		ICalendar weeklySummaryCal = new ICalendar();
		switch(fileType){
			case("ICS"):
				weeklySummaryCal = createWeeklySummaryCalendarICS(project);
				break;
			case("CSV"):
				//TODO: do nothing for now
				break;
			default:
				break;
		}
		return weeklySummaryCal;
	}
	
	
	public ICalendar createWeeklySummaryCalendarICS(ProjectFile project){
		ICalendar weeklySummaryCal = new ICalendar();
		//ColourGiver colourGiver = new ColourGiver();
		Map<Integer,String> mondayEvents = new HashMap<Integer,String>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		int count = 1;
		for(Task task : project.getTasks()){
			//This task is an activity if it fulfills 3 conditions:
			//1. If taskname does not contain milestone and
			//2. If task has a parent task and
			//3. If task has no children task
			
			if(!task.getName().contains("Milestone") && 
					task.getParentTask()!=null && 
					task.getChildTasks().size()==0){
				
				//Create monday summary
				WeeklySummaryHelper wsh = new WeeklySummaryHelper();
				wsh.setDateListOfDay(DayOfWeek.MONDAY,task.getStart(),task.getFinish());
				//System.out.println(task.getName() + " start " +  task.getStart() + " fin "+ task.getFinish());
				ArrayList<Integer> mondayList = wsh.getDayDateList();
				
				//Set<Integer> set = new HashSet<Integer>(mondayList);
				
				//System.out.println(task.getName() + " List: " +mondayList);
				if(mondayList.size()==0) {
					//do nothing 
				}else{
					String newEventString = "Task " + count + " [" + task.getParentTask().getName() + "]: " + task.getName() + "\n";
					
					//for each monday in mondaylist
					for(Integer mondayDateInt : mondayList){
						//if monday is in hashmap, append to the value
						if(mondayEvents.containsKey(mondayDateInt)){	
							String appendedValue = mondayEvents.get(mondayDateInt) + newEventString;
							mondayEvents.put(mondayDateInt, appendedValue);
						//if monday not in hashmap, add event
						}else{
							mondayEvents.put(mondayDateInt, newEventString);
						}
					}		
				}
				count++;
			
			}
		}
		
		
				
		//Map<Integer,String> mondayEvents = new HashMap<Integer,String>();
		Iterator it = mondayEvents.entrySet().iterator();
		WeeklySummaryHelper wsh2 = new WeeklySummaryHelper();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        Integer mondayDateInt = (Integer) pair.getKey();
	        String mondaySummaryDescription = (String) pair.getValue();
	        
	        //System.out.println("Key: " + pair.getKey() +" Value: "+ pair.getValue());
	        
	        VEvent newEvent = new VEvent();
	        Summary summary = newEvent.setSummary("Monday Summary: " + mondayDateInt);
	        summary.setLanguage("en-us");
	        Duration duration = new Duration.Builder().hours(1).build();
	        Date startDate = wsh2.convertToDateViaInstant(LocalDate.parse(mondayDateInt.toString(),formatter));
	        
	        newEvent.setSummary(summary);
	        newEvent.setDateStart(startDate);
	        newEvent.setDuration(duration);
	        newEvent.setDescription(mondaySummaryDescription);
	        
	        weeklySummaryCal.addEvent(newEvent);
	        it.remove(); // avoids a ConcurrentModificationException
	    }
				
		return weeklySummaryCal;
	}
	
	public ICalendar createReminderCalendarICS(ProjectFile project){
		ICalendar reminderCal = new ICalendar();
		ColourGiver colourGiver = new ColourGiver();
		WeeklySummaryHelper wsh = new WeeklySummaryHelper();
		
		for(Task task : project.getTasks()){
			//This task is an activity if it fulfills 3 conditions:
			//1. If taskname does not contain milestone and
			//2. If task has a parent task and
			//3. If task has no children task
			if(!task.getName().contains("Milestone") && 
					task.getParentTask()!=null && 
					task.getChildTasks().size()==0){
		
				VEvent newEvent = new VEvent();
				//Get parent and add as category
				String parentCategory = task.getParentTask().getName();
				//remove all commas from parentCategory so it does not mess with the categories
				parentCategory = parentCategory.replaceAll(",","");
				String colourCategory = colourGiver.getColourCategory(parentCategory);
	
				ArrayList<String> categories = new ArrayList<String>();
				categories.add(colourCategory);
				categories.add(parentCategory);
				
				newEvent.addCategories(categories);
				Summary summary2 = newEvent.setSummary("In 3 days: " + task.getName());
				summary2.setLanguage("en-us");
				
				LocalDate localDateMinus3 = wsh.convertToLocalDateViaInstant(task.getStart()).minusDays(3);
				Date dateMinus3 = wsh.convertToDateViaInstant(localDateMinus3);
				Duration duration = new Duration.Builder().hours(1).build();
				
				newEvent.setDateStart(dateMinus3);
				newEvent.setDuration(duration);
				reminderCal.addEvent(newEvent);
			}
		}
		return reminderCal;
	}
	
	
	public ICalendar createOverviewCalendarICS(ProjectFile project){
		ICalendar overviewCal = new ICalendar(); 
		
		ColourGiver colourGiver = new ColourGiver();
		
		for(Task task : project.getTasks()){
			//This task is an activity if it fulfills 3 conditions:
			//1. If taskname does not contain milestone and
			//2. If task has a parent task and
			//3. If task has no children task
			if(!task.getName().contains("Milestone") && 
					task.getParentTask()!=null && 
					task.getChildTasks().size()==0){
				
				VEvent newEvent = new VEvent();
				
				//Get parent and add as category
				String parentCategory = task.getParentTask().getName();
				//remove all commas from parentCategory so it does not mess with the categories
				parentCategory = parentCategory.replaceAll(",","");
				
				
				// Lookup colour hashtable for parent category, if the 
				// category exists use that colour, if it doesnt, get a new colour.
				
				// if activity, get parent name, label category as parent name.
				
				String colourCategory = colourGiver.getColourCategory(parentCategory);
				
				ArrayList<String> categories = new ArrayList<String>();
				categories.add(colourCategory);
				categories.add(parentCategory);
				
				newEvent.addCategories(categories);
				
				Summary newSummary = newEvent.setSummary(task.getName());
				newSummary.setLanguage("en-us");
				
				//get task start and end date
				newEvent.setDateStart(task.getStart());
				newEvent.setDateEnd(task.getFinish());
				
				overviewCal.addEvent(newEvent);
			}	
		}
		return overviewCal;
	}
}
