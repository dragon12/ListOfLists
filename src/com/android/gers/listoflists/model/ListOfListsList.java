package com.android.gers.listoflists.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.android.gers.listoflists.ListOfLists;

import android.util.Log;

public class ListOfListsList {
	private long id;
	private String name;
	private Date creationDate;
	private Boolean deleted;

	private static SimpleDateFormat dbStringToDateParser;
	private static SimpleDateFormat dateToStringFormatter;
	
	static {
		dbStringToDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateToStringFormatter = new SimpleDateFormat("yyyy-MM-dd");
	}
	
	public ListOfListsList(ListOfListsList other) {
		this.id = other.id;
		this.name = other.name;
		this.creationDate = other.creationDate;
		this.deleted = other.deleted;
	}
	public ListOfListsList(String name) {
		this(-1, name, null, false);
	}
	
	public ListOfListsList(long id, String name, String creationDate, Boolean deleted) {
		this.id = id;
		this.name = name;
		setDate(creationDate);
		this.deleted = deleted;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDate(String creationDate) {
		try {
			this.creationDate = 
					(creationDate == null 
						? null
						: dbStringToDateParser.parse(creationDate));
			
		} catch (Exception e){
			Log.e(ListOfLists.LOG_NAME, "Failed to parse date from " + creationDate);
		}
	}
	
	public String getCreationDate() { 
		return dbStringToDateParser.format(creationDate);
	}
	
	public String getDisplayDate() {
		return dateToStringFormatter.format(creationDate);
	}
	
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	public Boolean getDeleted() {
		return deleted;
	}
	
	@Override
	public String toString() {
		return String.format("id %d, name %s, creationDate %s, isDeleted %s", id, name, creationDate, deleted.toString());
	}
	
}
