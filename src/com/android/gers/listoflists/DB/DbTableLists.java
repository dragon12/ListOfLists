package com.android.gers.listoflists.DB;

import com.android.gers.listoflists.ListOfLists;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

public class DbTableLists extends DbTableBase {
	public static final String TABLE_NAME = "lists";

	public static final String COL_ID = BaseColumns._ID;
	public static final int COL_IDX_ID = 0;
	
	public static final String COL_NAME = "name";
	public static final int COL_IDX_NAME = 1;
	
	public static final String COL_CREATION_DATE = "creation_date";
	public static final int COL_IDX_CREATION_DATE = 2;
	
	public static final String COL_IS_DELETED = "is_deleted";
	public static final int COL_IDX_IS_DELETED = 3;
			
	public static final String[] Columns = {COL_ID, COL_NAME, COL_CREATION_DATE, COL_IS_DELETED};

	public DbTableLists() {
		super(TABLE_NAME);
	}
	
	public long insert(SQLiteDatabase db, String listName) {
		ContentValues kvps = new ContentValues();
		
		kvps.put(COL_NAME, listName);
		kvps.put(COL_IS_DELETED, 0);
		
		return insert(db, kvps); 
	}
	
	public long insert(SQLiteDatabase db, ContentValues kvps) {
		return db.insert(TABLE_NAME, null, kvps);
	}
	
	public Boolean update(SQLiteDatabase db, long id, ContentValues kvps) {
		return db.update(TABLE_NAME, kvps, COL_ID + " = " + id, null) == 1;
	}
	
	public Cursor getAllLists(SQLiteDatabase db) {
		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		qb.appendWhere(COL_IS_DELETED + " = '0'");
		
		return qb.query(db, Columns, null, null, null, null, COL_ID);
	}
	
	public Cursor getListById(SQLiteDatabase db, long id) {
		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		qb.appendWhere(COL_ID + " = " + id);
		
		return qb.query(db, Columns, null, null, null, null, COL_ID);		
	}
	
	public Boolean deleteListById(SQLiteDatabase db, long id) {
		return db.delete(TABLE_NAME, COL_ID + " = " + id, null) == 1;
	}
	

	@Override	
	public void create(SQLiteDatabase db) {
		Log.d(ListOfLists.LOG_NAME, "table " + TABLE_NAME + " doesn't exist, creating table");
		String sql = 
				"CREATE TABLE " + TABLE_NAME + "(" + 
						COL_ID + " integer primary key autoincrement, " +
						COL_NAME + " text not null, " +
						COL_CREATION_DATE + " text not null DEFAULT (datetime('now','localtime')), " +
						COL_IS_DELETED + " boolean not null DEFAULT 0" +
								")";
		Log.d(ListOfLists.LOG_NAME, "Creating lists table with sql command: " + sql);
		
		db.execSQL(sql);
	}

}


