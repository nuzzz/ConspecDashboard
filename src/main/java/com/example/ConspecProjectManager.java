package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Summary;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.reader.UniversalProjectReader;

public class ConspecProjectManager {
	private String projectFilename;
	private ProjectFile projectData;
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
		ArrayList<ICalendar> calendars = new ArrayList<ICalendar>();
		calendars.add(overviewCal);
		//ICalendar reminder3Cal = new ICalendar();
		//ICalendar monsummaryCal = new ICalendar();
		
		return calendars;
	}
	
	public ICalendar createOverviewCalendar(ProjectFile project, String fileType){
		ICalendar overviewCal = new ICalendar();
		switch(fileType){
			case("ICS"):
				overviewCal = createOverviewCalendarICS(project);
				break;
			case("CSV"):
				//do nothing for now
				break;
			default:
				//invalid file type
				break;
		}
		return overviewCal;
	}
	
	public ICalendar createOverviewCalendarICS(ProjectFile project){
		ICalendar overviewCal = new ICalendar(); 
		ColourGiver colourGiver = new ColourGiver();
		
		System.out.println("project tasks amount: " +project.getTasks().size());
		
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
