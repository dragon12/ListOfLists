package com.android.gers.listoflists.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.gers.listoflists.ListOfLists;
import com.android.gers.listoflists.DB.DbTableListItems;
import com.android.gers.listoflists.DB.DbTableLists;
import com.android.gers.listoflists.DB.ListOfListsDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ListDataSource {

	private ListOfListsDb dbHelper;
	private SQLiteDatabase db;
	
	public ListDataSource(Context context, ListOfListsDb dbHelper) {
		this.dbHelper = dbHelper;
	}

	public void open() throws SQLException {
		Log.d(ListOfLists.LOG_NAME, "Opening db");
		db = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		Log.d(ListOfLists.LOG_NAME, "Closing db");
		db.close();
	}
	
	public ListOfListsList createList(ListOfListsList newList) throws Exception {
		Log.d(ListOfLists.LOG_NAME, "Inserting new shopping list with name: " + newList.getName());
		
		Log.d(ListOfLists.LOG_NAME, "before insert list");
		long insertId = dbHelper.insertList(db, newList.getName());
		Log.d(ListOfLists.LOG_NAME, "after insert list");
		
		Cursor queryCursor = dbHelper.getListById(db, insertId);
		queryCursor.moveToFirst();
		if (queryCursor.isAfterLast()) {
			throw new Exception("We added an item but then it wasn't there!");
		}
		ListOfListsList retVal = cursorToListOfListsList(queryCursor);
		Log.d(ListOfLists.LOG_NAME, "Inserted new list: " + retVal.toString());
		
		queryCursor.close();
		return retVal;
	}

	public Boolean updateList(ListOfListsList editList) throws Exception {
		Log.d(ListOfLists.LOG_NAME, "Updating list: " + editList.toString());
		
		ContentValues kvps = listOfListsListToContentValues(editList);
		Boolean retVal = dbHelper.updateList(db, editList.getId(), kvps);
		
		Log.d(ListOfLists.LOG_NAME, "Updated list");
		return retVal;
	}
	
	public ListOfListsListItem createItem(ListOfListsListItem newItem) throws Exception {
		Log.d(ListOfLists.LOG_NAME, "Inserting new item: " + newItem.toString());
		
		ContentValues kvps = listOfListsListItemToContentValues(newItem);
		long insertId = dbHelper.insertItem(db, kvps);
		
		Cursor queryCursor = dbHelper.getItemsByID(db, insertId);
		
		queryCursor.moveToFirst();
		if (queryCursor.isAfterLast()) {
			throw new Exception("We added an item but then it wasn't there!");
		}
		ListOfListsListItem retVal = cursorToListOfListsListItem(queryCursor);
		Log.d(ListOfLists.LOG_NAME, "Inserted new item: " + retVal.toString());
		
		queryCursor.close();
		return retVal;
	}
	
	public Boolean updateItem(ListOfListsListItem editItem) throws Exception {
		Log.d(ListOfLists.LOG_NAME, "Updating item: " + editItem.toString());
		
		ContentValues kvps = listOfListsListItemToContentValues(editItem);
		Boolean retVal = dbHelper.updateItem(db, editItem.getId(), kvps);
		
		Log.d(ListOfLists.LOG_NAME, "Updated item");
		return retVal;
	}
	
	
	public void deleteList(ListOfListsList deleteList) {
		Log.d(ListOfLists.LOG_NAME, "About to delete this shopping list: " + deleteList.toString());
		
		if(dbHelper.deleteListById(db, deleteList.getId())) {
			Log.d(ListOfLists.LOG_NAME, "Successfully deleted list");
		} else {
			Log.e(ListOfLists.LOG_NAME, "Couldn't delete list with id " + deleteList.getId() + "!");
		}
	}
	
	public void deleteItem(ListOfListsListItem deleteItem) {
		if(dbHelper.deleteItemById(db, deleteItem.getId())) {
			Log.d(ListOfLists.LOG_NAME, "Successfully deleted item");
		} else {
			Log.e(ListOfLists.LOG_NAME, "Couldn't delete item with id " + deleteItem.getId() + "!");
		}
	}
	
	public List<ListOfListsList> getLists() {
		List<ListOfListsList> lists = new ArrayList<ListOfListsList>();
		Cursor queryCursor = dbHelper.getAllLists(db);
		
		queryCursor.moveToFirst();
		while(!queryCursor.isAfterLast()) {
			lists.add(cursorToListOfListsList(queryCursor));
			queryCursor.moveToNext();
		}
		queryCursor.close();
		
		return lists;
	}
	
	public HashMap<Long, ListOfListsListItemStats> getListOfListsListItemStats() {
		HashMap<Long, ListOfListsListItemStats> stats = new HashMap<Long, ListOfListsListItemStats>();
		
		Cursor queryCursor = dbHelper.getListOfListsListItemStats(db);
		
		queryCursor.moveToFirst();
		while(!queryCursor.isAfterLast()) {
			long listId = queryCursor.getLong(DbTableListItems.COL_QUERY_STATS_LIST_ID);
			int count = queryCursor.getInt(DbTableListItems.COL_QUERY_STATS_COUNT_STATUS);
			
			ListOfListsListItemStats stat = stats.get(listId);
			if (stat == null) {
				stat = new ListOfListsListItemStats(listId);
				stats.put(stat.listId, stat);
			}
			stat.count = count;
			
			queryCursor.moveToNext();
		}
		queryCursor.close();
		
		return stats;
	}

	public ListOfListsList getList(long id) {
		Cursor queryCursor = dbHelper.getListById(db, id);
		
		ListOfListsList retVal = null;
		
		queryCursor.moveToFirst();
		if(!queryCursor.isAfterLast()) {
			retVal = cursorToListOfListsList(queryCursor);
			queryCursor.moveToNext();
		}
		queryCursor.close();
		
		return retVal;
	}
	
	public List<ListOfListsListItem> getListOfListsListItems(long listId) {
		List<ListOfListsListItem> items = new ArrayList<ListOfListsListItem>();
		Cursor queryCursor = dbHelper.getItemsByListID(db, listId);
		
		queryCursor.moveToFirst();
		while(!queryCursor.isAfterLast()) {
			items.add(cursorToListOfListsListItem(queryCursor));
			queryCursor.moveToNext();
		}
		queryCursor.close();
		
		return items;
	}

	private static ContentValues listOfListsListToContentValues(ListOfListsList list) {
		ContentValues kvps = new ContentValues();
		kvps.put(DbTableLists.COL_NAME, list.getName());
		kvps.put(DbTableLists.COL_CREATION_DATE, list.getCreationDate());
		kvps.put(DbTableLists.COL_IS_DELETED, list.getDeleted() ? 1 : 0);
		return kvps;
	}
	
	private static ContentValues listOfListsListItemToContentValues(ListOfListsListItem item) {
		ContentValues kvps = new ContentValues();
		kvps.put(DbTableListItems.COL_LIST_ID, item.getListId());
		kvps.put(DbTableListItems.COL_NAME, item.getName());
		kvps.put(DbTableListItems.COL_RATING, item.getRating());
		return kvps;
	}
	
	private static ListOfListsList cursorToListOfListsList(Cursor cursor) {
		return new ListOfListsList(
				cursor.getInt(DbTableLists.COL_IDX_ID), 
				cursor.getString(DbTableLists.COL_IDX_NAME),
				cursor.getString(DbTableLists.COL_IDX_CREATION_DATE),
				cursor.getInt(DbTableLists.COL_IDX_IS_DELETED) > 0
				);
	}

	private static ListOfListsListItem cursorToListOfListsListItem(Cursor cursor) {
		return new ListOfListsListItem(
				cursor.getInt(DbTableListItems.COL_IDX_ID),
				cursor.getInt(DbTableListItems.COL_IDX_LIST_ID),
				cursor.getString(DbTableListItems.COL_IDX_NAME),
				cursor.getDouble(DbTableListItems.COL_IDX_RATING)
				);
	}
	
}
