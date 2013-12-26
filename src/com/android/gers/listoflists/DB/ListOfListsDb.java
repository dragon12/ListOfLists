package com.android.gers.listoflists.DB;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.android.gers.listoflists.ListOfLists;
import com.android.gers.utils.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ListOfListsDb extends SQLiteOpenHelper {

	private DbTableLists dbTableLists;
	private DbTableListItems dbTableItems;
	
	private TestDBGenerator debugHelper;
	Context myContext;
	
	public ListOfListsDb(Context context) {
		super(context, ListOfLists.DB.NAME, null, ListOfLists.DB.DB_VERSION);

		this.myContext = context;
		dbTableLists = new DbTableLists();
		dbTableItems = new DbTableListItems(DbTableLists.TABLE_NAME, DbTableLists.COL_ID);
		
		debugHelper = new TestDBGenerator(context, this);
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createIfNotPresent(db, dbTableLists);
		createIfNotPresent(db, dbTableItems);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		recreate(db);
	}
	
	public void deleteDb() {
		close();
		myContext.deleteDatabase(ListOfLists.DB.NAME);
	}
	
	public void createTestDb() throws IOException {
		debugHelper.CreateTestDB();
	}
	
	public boolean exportDbAsXml(String fileName) {
		BufferedOutputStream outputStream;
		try {
			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
				
			outputStream = FileUtils.OpenFileForWriting(fileName);
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		DbToXml dbToXml = new DbToXml(getReadableDatabase(), outputStream, ListOfLists.DB.DB_VERSION);
		
		dbToXml.Execute();
		
		return true;
	}
	
	private void recreate(SQLiteDatabase db) {
		dbTableItems.drop(db);
		dbTableLists.drop(db);
		
		onCreate(db);
	}
	
	public boolean importDbAsXml(String fileName) throws Exception {
		InputStream inputStream;
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				Log.e(ListOfLists.LOG_NAME, "file " + file + " does not exist");
			}
				
			inputStream = FileUtils.GetFileStream(fileName);
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		DbFromXml dbFromXml = new DbFromXml(inputStream);
		HashMap<String, List<ContentValues>> tablesToRows = dbFromXml.Execute();
		
		if (tablesToRows.size() == 0) {
			throw new Exception("Can't import empty xml file");
		}
		
		SQLiteDatabase db = getWritableDatabase();

		recreate(db);
		for (Entry<String, List<ContentValues>> item : tablesToRows.entrySet()) {
			for (ContentValues row : item.getValue()) {
				if (item.getKey().equals(DbTableLists.TABLE_NAME)) {
					insertList(db, row);
				} else if (item.getKey().equals(DbTableListItems.TABLE_NAME)) {
					insertItem(db, row);
				} else {
					throw new Exception("Unknown table: " + item.getKey());
				}
			}
		}
		return true;
	}
	
	public long insertList(SQLiteDatabase db, String listName) {
		return dbTableLists.insert(db, listName); 
	}
	
	public long insertList(SQLiteDatabase db, ContentValues list) {
		return dbTableLists.insert(db, list);
	}

	public Boolean updateList(SQLiteDatabase db, long id, ContentValues kvps) {
		return dbTableLists.update(db, id, kvps);
	}	
	
	
	public long insertItem(SQLiteDatabase db, ContentValues kvps) {
		return dbTableItems.insert(db, kvps);
	}
	
	public Boolean updateItem(SQLiteDatabase db, long id, ContentValues kvps) {
		return dbTableItems.update(db, id, kvps);
	}	
	
	public Boolean updateByListId(SQLiteDatabase db, long listId, ContentValues kvps) {
		return dbTableItems.updateByListId(db, listId, kvps);
	}
	
	public Boolean deleteByListId(SQLiteDatabase db, long listId, ContentValues whereKvps) {
		return dbTableItems.deleteItemsByListIdWhere(db, listId, whereKvps);
	}

	public Cursor getAllLists(SQLiteDatabase db) {
		return dbTableLists.getAllLists(db);
	}
	
	public Cursor getListById(SQLiteDatabase db, long id) {
		return dbTableLists.getListById(db, id);		
	}
	
	public Cursor getItemsByListID(SQLiteDatabase db, long listId) {
		return dbTableItems.getItemsByListId(db, listId);
	}
	
	public Cursor getItemsByID(SQLiteDatabase db, long itemId) {
		return dbTableItems.getItemById(db, itemId);
	}

	public Cursor getListOfListsListItemStats(SQLiteDatabase db) {
		return dbTableItems.getItemStatsByListId(db);
	}
	
	public Boolean deleteListById(SQLiteDatabase db, long id) {
		if (deleteItemsByListId(db, id)) {
			Log.d(ListOfLists.LOG_NAME, "calling deleteListById");
			return dbTableLists.deleteListById(db, id);	
		}
		return false;
	}

	public Boolean deleteItemsByListId(SQLiteDatabase db, long listId) {
		Log.d(ListOfLists.LOG_NAME, "deleteItemsByListId");
		return dbTableItems.deleteItemsByListId(db, listId);
	}
	
	public Boolean deleteItemById(SQLiteDatabase db, long id) {
		return dbTableItems.deleteItemsById(db, id);
	}
	
	private static void createIfNotPresent(SQLiteDatabase db, DbTableBase dbTable) {
		if (!dbTable.exists(db)) {
			Log.d(ListOfLists.LOG_NAME, "table " + dbTable.getName() + " doesn't exist, creating");
			dbTable.create(db);
		}
	}

}
